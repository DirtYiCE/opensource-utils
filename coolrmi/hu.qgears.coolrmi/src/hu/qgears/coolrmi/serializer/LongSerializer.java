package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class LongSerializer extends TypeSerializer {
	public LongSerializer() {
		super(Type.Int64, Long.class, long.class);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is) {
		return Long.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.write64(os, (Long) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return Utils.read64(is);
	}

}
