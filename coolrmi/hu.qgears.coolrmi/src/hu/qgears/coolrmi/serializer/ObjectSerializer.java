package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import sun.reflect.ReflectionFactory;

class ObjectSerializer extends TypeSerializer {
	public ObjectSerializer() {
		super(TypeId.Object, Object.class, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return typ.getCls() == Object.class ||
				PortableSerializable.class.isAssignableFrom(typ.getCls());
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		super.writeType(serializer, os, typ);
		serializer.writeClassName(os, typ);
	}

	@Override
	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		return serializer.readClassName(is);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Class<?> cls = o.getClass();

		while (PortableSerializable.class.isAssignableFrom(cls)) {
			Field[] fields = cls.getDeclaredFields();

			for (Field f : fields)  {
				int mods = f.getModifiers();
				if (!Modifier.isTransient(mods) && !Modifier.isStatic(mods)) {
					Utils.writeString(os, f.getName());
					f.setAccessible(true);
					JavaType x = new JavaType(f.getType(), f.getGenericType());
					Object fieldVal = null;
					try {
						fieldVal = f.get(o);
					} catch (IllegalArgumentException e) {
						// shouldn't happen
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						// shouldn't happen
						throw new RuntimeException(e);
					}
					serializer.serialize(os, fieldVal, x);
				}
			}

			cls = cls.getSuperclass();
		}
		Utils.writeString(os, null);
	}

	private static Object getObject(Class<?> cls) throws NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<?> parentCls = cls;
		while (PortableSerializable.class.isAssignableFrom(parentCls)) {
			parentCls = parentCls.getSuperclass();
		}
		Constructor<?> ctor = parentCls.getDeclaredConstructor();
		@SuppressWarnings("restriction")
		Constructor<?> ctor2 = ReflectionFactory.getReflectionFactory()
				.newConstructorForSerialization(cls, ctor);
		ctor2.setAccessible(true);
		return ctor2.newInstance();
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		Class<?> cls = typ.getCls();
		Object instance = getObject(cls);

		String fieldName;
		while ((fieldName = Utils.readString(is)) != null) {
			Field field = null;
			while (field == null) {
				try {
					field = cls.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					cls = cls.getSuperclass();
				}
			}
			field.setAccessible(true);
			JavaType x = new JavaType(field.getType(), field.getGenericType());
			field.set(instance, serializer.deserialize(is, x));
		}

		return instance;
	}

	@Override
	public boolean isPolymorphic() {
		return true;
	}
}
