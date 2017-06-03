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
		super(Type.Object, Object.class, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, Class<?> o) {
		return o == Object.class ||
				PortableSerializable.class.isAssignableFrom(o);
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			Class<?> cls) throws IOException {
		super.writeType(serializer, os, cls);
		Utils.writeString(os, cls.getName());
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		String className = Utils.readString(is);
		return serializer.getClassLoader().loadClass(className);
	}

	@Override
	public void serialize(PortableSerializer serializer,
			Object o, OutputStream os)
			throws IOException {
		Class<?> cls = o.getClass();

		while (PortableSerializable.class.isAssignableFrom(cls)) {
			Field[] fields = cls.getDeclaredFields();
			//Arrays.sort(fields);

			for (Field f : fields)  {
				int mods = f.getModifiers();
				if (!Modifier.isTransient(mods) && !Modifier.isStatic(mods)) {
					Utils.writeString(os, f.getName());
					f.setAccessible(true);
					Class<?> x = serializer.getClassForSerialization(f.getType());
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
		Utils.writeString(os, "");
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
			Class<?> cls)
			throws Exception {
		Object instance = getObject(cls);

		String fieldName;
		while (!(fieldName = Utils.readString(is)).isEmpty()) {
			Field field = null;
			while (field == null) {
				try {
					field = cls.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					cls = cls.getSuperclass();
				}
			}
			field.setAccessible(true);
			Class<?> x = serializer.getClassForSerialization(field.getType());
			field.set(instance, serializer.deserialize(is, x));
		}

		return instance;
	}

	@Override
	public boolean isPolymorphic() {
		return true;
	}
}
