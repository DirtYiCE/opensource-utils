package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class LongSerializer extends TypeSerializer {
	public LongSerializer() {
		super(TypeId.Int64, Long.class, long.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.write64(os, (Long) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return Utils.read64(is);
	}

}
