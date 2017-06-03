package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class TypeSerializer {
	final Type type;
	final Class<?> serializedClass, primitiveClass;

	public TypeSerializer(Type type, Class<?> serializedClass,
			Class<?> primitveClass) {
		this.type = type;
		this.serializedClass = serializedClass;
		this.primitiveClass = primitveClass;
	}

	public void writeType(PortableSerializer serializer, OutputStream os,
			Class<?> cls) throws IOException {
		os.write(type.ordinal());
	}

	public abstract Class<?> readType(PortableSerializer serializer,
			InputStream is)
			throws IOException, ClassNotFoundException;

	public abstract void serialize(PortableSerializer serializer, Object o,
			OutputStream os) throws IOException;

	public abstract Object deserialize(PortableSerializer serializer,
			InputStream is, Class<?> cls)
			throws Exception;

	public boolean canSerializeIsSpecial() {
		return false;
	}

	public boolean canSerialize(PortableSerializer serializer, Class<?> o) {
		return o.equals(serializedClass) || o.equals(primitiveClass);
	}

	public boolean isPolymorphic() {
		return false;
	}
}
