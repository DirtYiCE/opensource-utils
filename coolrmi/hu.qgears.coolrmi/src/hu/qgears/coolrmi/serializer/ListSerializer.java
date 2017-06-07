package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListSerializer extends TypeSerializer {

	public ListSerializer() {
		super(TypeId.List, null, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return List.class.isAssignableFrom(typ.getCls());
	}

	private Type getElementType(JavaType typ) {
		Type[] generic = typ.getGenericTypes();
		return generic.length >= 1 ? generic[0] : Object.class;
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		super.writeType(serializer, os, typ);
		Utils.writeString(os, serializer.getPortableClassName(getElementType(typ)));
	}

	@Override
	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		Class<?> items = serializer.loadClass(Utils.readString(is));
		return new JavaType(ArrayList.class, new Type[] { items });
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		if (o == null) {
			Utils.write32(os, -1);
			return;
		}
		List<?> lst = (List<?>) o;
		Utils.write32(os, lst.size());

		JavaType elemType = new JavaType(getElementType(typ));
		for (Object el : lst) {
			serializer.serialize(os, el, elemType);
		}
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		int len = Utils.read32(is);
		if (len == -1) {
			return null;
		}

		List<Object> lst = new ArrayList<Object>(len);
		JavaType elemType = new JavaType(getElementType(typ));

		for (int i = 0; i < len; ++i) {
			lst.add(serializer.deserialize(is, elemType));
		}
		return lst;
	}

}
