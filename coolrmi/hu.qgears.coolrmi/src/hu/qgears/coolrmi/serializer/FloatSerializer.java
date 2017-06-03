package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FloatSerializer extends TypeSerializer {
	public FloatSerializer() {
		super(Type.Float, Float.class, float.class);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is) {
		return Float.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.write32(os, Float.floatToRawIntBits((Float) o));
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return Float.intBitsToFloat(Utils.read32(is));
	}

}
