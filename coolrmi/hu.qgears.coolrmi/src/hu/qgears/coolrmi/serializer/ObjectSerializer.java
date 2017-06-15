package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sun.reflect.ReflectionFactory;

public class ObjectSerializer extends TypeSerializer {
	public ObjectSerializer() {
		super(TypeId.Object, Object.class, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		Class<?> cls = typ.getCls();
		return cls.equals(Object.class) ||
				PortableSerializable.class.isAssignableFrom(cls) ||
				ICustomPortableSerializable.class.isAssignableFrom(cls);
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

	private static class CacheEntry {
		public String name;
		public Field field;
		public JavaType javaType;

		public CacheEntry(String name, Field field) {
			this.name = name;
			this.field = field;
			this.javaType = new JavaType(field.getType(), field.getGenericType());
		}
	}

	private static ConcurrentHashMap<Class<?>, Map<String, CacheEntry>> TYPE_CACHE =
			new ConcurrentHashMap<Class<?>, Map<String, CacheEntry>>();

	private static Map<String, CacheEntry> getTypeCache(Class<?> cls) {
		Map<String, CacheEntry> ret = TYPE_CACHE.get(cls);
		if (ret != null) {
			return ret;
		}

		ret = new HashMap<String, CacheEntry>();
		Field[] fields = cls.getDeclaredFields();
		for (Field f : fields) {
			int mods = f.getModifiers();
			if (Modifier.isTransient(mods) || Modifier.isStatic(mods)) {
				continue;
			}

			PortableFieldName annot = f.getAnnotation(PortableFieldName.class);
			String name;
			if (annot != null) {
				name = annot.name();
			} else {
				name = f.getName();
			}

			f.setAccessible(true);
			if (ret.put(name, new CacheEntry(name, f)) != null) {
				throw new RuntimeException("Duplicate field name '" + name
						+ "' in class " + cls.getName());
			}
		}

		// ignore if another thread already added it
		TYPE_CACHE.putIfAbsent(cls, ret);
		return ret;
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		if (o instanceof ICustomPortableSerializable) {
			((ICustomPortableSerializable) o).serialize(serializer, os);
		} else {
			defaultSerialize(serializer, os, o, typ.getCls());
		}
	}

	public static void defaultSerialize(PortableSerializer serializer,
			OutputStream os, Object o, Class<?> cls)
			throws IOException, RuntimeException {
		while (PortableSerializable.class.isAssignableFrom(cls)) {
			Map<String, CacheEntry> cache = getTypeCache(cls);

			for (CacheEntry c : cache.values())  {
				Utils.writeString(os, c.name);
				Object fieldVal = null;
				try {
					fieldVal = c.field.get(o);
				} catch (IllegalArgumentException e) {
					// shouldn't happen
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					// shouldn't happen
					throw new RuntimeException(e);
				}
				serializer.serialize(os, fieldVal, c.javaType);
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

	private static final Class<?>[] DESERIALIZER_ARGS = new Class<?>[] {
		PortableSerializer.class,
		InputStream.class
	};

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		Class<?> cls = typ.getCls();

		if (ICustomPortableSerializable.class.isAssignableFrom(cls)) {
			try {
				Constructor<?> ctor = cls.getDeclaredConstructor(DESERIALIZER_ARGS);
				ctor.setAccessible(true);
				return ctor.newInstance(serializer, is);
			} catch (NoSuchMethodException e) {
				Method meth = cls.getDeclaredMethod("deserialize", DESERIALIZER_ARGS);
				return meth.invoke(null, serializer, is);
			}
		} else {
			Object instance = getObject(cls);
			defaultDeserialize(serializer, is, instance, cls);
			return instance;
		}
	}

	public static void defaultDeserialize(PortableSerializer serializer,
			InputStream is, Object o, Class<?> cls) throws Exception {
		String fieldName;
		Map<String, CacheEntry> cache = getTypeCache(cls);
		while ((fieldName = Utils.readString(is)) != null) {
			CacheEntry e;
			while ((e = cache.get(fieldName)) == null) {
				cls = cls.getSuperclass();
				cache = getTypeCache(cls);
			}
			e.field.set(o, serializer.deserialize(is, e.javaType));
		}
	}

	@Override
	public boolean isPolymorphic() {
		return true;
	}
}
