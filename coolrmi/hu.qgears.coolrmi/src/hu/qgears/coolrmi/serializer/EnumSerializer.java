package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class EnumSerializer extends TypeSerializer {
	public EnumSerializer() {
		super(TypeId.Enum, null, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return typ.getCls().isEnum();
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		super.writeType(serializer, os, typ);
		Utils.writeString(os, serializer.getPortableClassName(typ));
	}

	@Override
	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		return new JavaType(serializer.loadClass(Utils.readString(is)));
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.writeString(os, ((Enum<?>) o).name());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		String value = Utils.readString(is);
		return Enum.valueOf((Class) typ.getCls(), value);
	}

}
