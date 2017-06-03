package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class BooleanSerializer extends TypeSerializer {
	public BooleanSerializer() {
		super(Type.Bool, Boolean.class, boolean.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.writeBool(os, (Boolean) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return Utils.readBool(is);
	}

}
