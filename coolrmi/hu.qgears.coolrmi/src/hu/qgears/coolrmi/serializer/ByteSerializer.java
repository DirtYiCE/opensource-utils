package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ByteSerializer extends TypeSerializer {
	public ByteSerializer() {
		super(Type.Int8, Byte.class, byte.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		os.write((Byte) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return (byte) is.read();
	}

}
