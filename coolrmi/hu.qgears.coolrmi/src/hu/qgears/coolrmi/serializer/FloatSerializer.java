package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FloatSerializer extends TypeSerializer {
	public FloatSerializer() {
		super(TypeId.Float, Float.class, float.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.write32(os, Float.floatToRawIntBits((Float) o));
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return Float.intBitsToFloat(Utils.read32(is));
	}

}
