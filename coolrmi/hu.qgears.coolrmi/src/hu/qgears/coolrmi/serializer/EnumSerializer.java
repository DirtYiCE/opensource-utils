package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class EnumSerializer extends TypeSerializer {
	public EnumSerializer() {
		super(Type.Enum, null, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, Class<?> o) {
		return o.isEnum();
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			Class<?> cls) throws IOException {
		super.writeType(serializer, os, cls);
		Utils.writeString(os, cls.getName());
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader)
			throws IOException, ClassNotFoundException {
		String className = Utils.readString(is);
		return classLoader.loadClass(className);
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.writeString(os, ((Enum<?>) o).name());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader, Class<?> cls) throws IOException {
		String value = Utils.readString(is);
		return Enum.valueOf((Class) cls, value);
	}

}
