package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DoubleSerializer extends TypeSerializer {
	public DoubleSerializer() {
		super(TypeId.Double, Double.class, double.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.write64(os, Double.doubleToRawLongBits((Double) o));
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return Double.longBitsToDouble(Utils.read64(is));
	}

}
