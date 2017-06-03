package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CharSerializer extends TypeSerializer {
	public CharSerializer() {
		super(Type.Char, Character.class, char.class);
	}

	@Override
	public Class<?> readType(PortableSerializer serializer, InputStream is) {
		return Character.class;
	}

	@Override
	public void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException {
		Utils.write16(os, (short) (char) (Character) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			Class<?> cls) throws IOException {
		return (char) Utils.read16(is);
	}

}
