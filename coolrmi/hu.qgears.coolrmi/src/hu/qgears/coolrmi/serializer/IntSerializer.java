package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class IntSerializer extends TypeSerializer {
	public IntSerializer() {
		super(Type.Int32, Integer.class, int.class);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader) {
		return Integer.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.write32(os, (Integer) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader, Class<?> cls) throws IOException {
		return Utils.read32(is);
	}

}
