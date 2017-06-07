package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ByteSerializer extends TypeSerializer {
	public ByteSerializer() {
		super(TypeId.Int8, Byte.class, byte.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		os.write((Byte) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return (byte) is.read();
	}

}
