package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

class ArraySerializer extends TypeSerializer {
	public ArraySerializer() {
		super(TypeId.Array, null, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return typ.getCls().isArray();
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		super.writeType(serializer, os, typ);
		Class<?> elems = typ.getCls().getComponentType();
		TypeSerializer elemSer = serializer.getSerializer(new JavaType(elems));

		elemSer.writeType(serializer, os, new JavaType(elems));
	}

	@Override
	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {

		TypeSerializer ser = serializer.getSerializer(is.read());
		JavaType elemCls = ser.readType(serializer, is);
		return new JavaType(Array.newInstance(elemCls.getCls(), 0));
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		if (o == null) {
			Utils.write32(os, -1);
			return;
		}

		int len = Array.getLength(o);
		Utils.write32(os, len);

		JavaType serCls = new JavaType(o.getClass().getComponentType());
		for (int i = 0; i < len; ++i) {
			serializer.serialize(os, Array.get(o, i), serCls);
		}
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		int len = Utils.read32(is);

		if (len == -1) {
			return null;
		}

		Class<?> elemCls = typ.getCls().getComponentType();
		JavaType loadCls = new JavaType(elemCls);


		Object ret = Array.newInstance(elemCls, len);
		for (int i = 0; i < len; ++i) {
			Array.set(ret, i, serializer.deserialize(is, loadCls));
		}

		return ret;
	}
}
