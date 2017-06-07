package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BooleanSerializer extends TypeSerializer {
	public BooleanSerializer() {
		super(TypeId.Bool, Boolean.class, boolean.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.writeBool(os, (Boolean) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return Utils.readBool(is);
	}

}
