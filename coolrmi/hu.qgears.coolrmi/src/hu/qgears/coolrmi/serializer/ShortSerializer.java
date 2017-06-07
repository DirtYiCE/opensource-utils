package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ShortSerializer extends TypeSerializer {
	public ShortSerializer() {
		super(TypeId.Int16, Short.class, short.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.write16(os, (Short) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return Utils.read16(is);
	}

}
