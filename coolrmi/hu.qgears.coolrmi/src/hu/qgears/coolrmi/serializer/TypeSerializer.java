package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class TypeSerializer {
	final TypeId type;
	final Class<?> serializedClass, primitiveClass;
	private final JavaType javaType;

	public TypeSerializer(TypeId type, Class<?> serializedClass,
			Class<?> primitveClass) {
		this.type = type;
		this.serializedClass = serializedClass;
		this.primitiveClass = primitveClass;
		this.javaType = new JavaType(
				primitiveClass != null ? primitiveClass : serializedClass);
	}

	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		os.write(type.ordinal());
	}

	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		return javaType;
	}

	public abstract void serialize(PortableSerializer serializer,
			OutputStream os, Object o, JavaType typ) throws IOException;

	public abstract Object deserialize(PortableSerializer serializer,
			InputStream is, JavaType typ) throws Exception;

	public boolean canSerializeIsSpecial() {
		return false;
	}

	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return typ.getCls().equals(serializedClass)
				|| typ.getCls().equals(primitiveClass);
	}

	public boolean isPolymorphic() {
		return false;
	}
}
