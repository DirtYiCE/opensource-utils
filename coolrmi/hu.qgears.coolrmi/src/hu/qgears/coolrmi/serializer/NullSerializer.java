package hu.qgears.coolrmi.serializer;

import java.io.InputStream;
import java.io.OutputStream;

class NullSerializer extends TypeSerializer {

	public NullSerializer() {
		super(TypeId.Null, null, null);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) {
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) {
		return null;
	}

}
