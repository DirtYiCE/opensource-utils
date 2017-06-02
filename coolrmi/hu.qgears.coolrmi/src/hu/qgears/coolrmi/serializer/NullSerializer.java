package hu.qgears.coolrmi.serializer;

import java.io.InputStream;
import java.io.OutputStream;

class NullSerializer extends TypeSerializer {

	public NullSerializer() {
		super(Type.Null, null, null);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader) {
		return null;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) {
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			ClassLoader classLoader, Class<?> cls) {
		return null;
	}

}
