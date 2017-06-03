package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

class ArraySerializer extends TypeSerializer {
	public ArraySerializer() {
		super(Type.Array, null, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, Class<?> o) {
		return o.isArray();
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os, Class<?> cls) throws IOException {
		super.writeType(serializer, os, cls);
		Class<?> elems = cls.getComponentType();
		TypeSerializer elemSer = serializer.getSerializer(elems);

		elemSer.writeType(serializer, os, elems);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {

		TypeSerializer ser = serializer.getSerializer(is.read());
		Class<?> elemCls = ser.readType(serializer, is);
		return Array.newInstance(elemCls, 0).getClass();
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		int len = Array.getLength(o);
		Utils.write32(os, len);

		Class<?> serCls = serializer.getClassForSerialization(o.getClass().getComponentType());
		for (int i = 0; i < len; ++i) {
			serializer.serialize(os, Array.get(o, i), serCls);
		}
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls)
			throws Exception {
		int len = Utils.read32(is);

		Class<?> elemCls = cls.getComponentType();
		Class<?> loadCls = serializer.getClassForSerialization(elemCls);


		Object ret = Array.newInstance(elemCls, len);
		for (int i = 0; i < len; ++i) {
			Array.set(ret, i, serializer.deserialize(is, loadCls));
		}

		return ret;
	}
}
