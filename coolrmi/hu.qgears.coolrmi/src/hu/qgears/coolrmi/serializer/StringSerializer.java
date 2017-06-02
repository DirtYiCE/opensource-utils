package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class StringSerializer extends TypeSerializer {
	public StringSerializer() {
		super(Type.String, String.class, null);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader) {
		return String.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.writeString(os, (String) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader, Class<?> cls) throws IOException {
		return Utils.readString(is);
	}

}
