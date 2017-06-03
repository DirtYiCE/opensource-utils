package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DoubleSerializer extends TypeSerializer {
	public DoubleSerializer() {
		super(Type.Double, Double.class, double.class);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is) {
		return Double.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.write64(os, Double.doubleToRawLongBits((Double) o));
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return Double.longBitsToDouble(Utils.read64(is));
	}

}
