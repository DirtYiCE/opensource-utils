package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class CharSerializer extends TypeSerializer {
	public CharSerializer() {
		super(TypeId.Char, Character.class, char.class);
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Utils.write16(os, (short) (char) (Character) o);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws IOException {
		return (char) Utils.read16(is);
	}

}
