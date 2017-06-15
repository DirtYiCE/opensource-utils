package hu.qgears.coolrmi.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.coolrmi.remoter.AbstractSerializer;
import hu.qgears.coolrmi.remoter.IReplaceSerializable;

public class PortableSerializer extends AbstractSerializer {
	private static final TypeSerializer[] serializers = new TypeSerializer[] {
		/*Null*/  new NullSerializer(),
		/*Bool*/  new BooleanSerializer(),
		/*Int8*/  new ByteSerializer(),
		/*Int16*/ new ShortSerializer(),
		/*Int32*/ new IntSerializer(),
		/*Int64*/ new LongSerializer(),
		/*Char*/  new CharSerializer(),
		/*Float*/ new FloatSerializer(),
		/*Double*/new DoubleSerializer(),
		/*Enum*/  new EnumSerializer(),
		/*String*/new StringSerializer(),
		/*Object*/new ObjectSerializer(),
		/*Array*/ new ArraySerializer(),
		/*Exception*/ new ExceptionSerializer(),
		/*List*/  new ListSerializer(),
	};

	private static final Map<Class<?>, TypeSerializer> classMap =
			new HashMap<Class<?>, TypeSerializer>();
	private static final List<TypeSerializer> specialSerializer =
			new ArrayList<TypeSerializer>();

	private Map<String, String> javaToPortableNameMap = new HashMap<String, String>();
	private Map<String, String> portableToJavaNameMap = new HashMap<String, String>();

	private Map<String, String> javaToPortableNamespaceMap = new HashMap<String, String>();
	private Map<String, String> portableToJavaNamespaceMap = new HashMap<String, String>();

	public void addMapping(String java, String portable) {
		javaToPortableNameMap.put(java, portable);
		portableToJavaNameMap.put(portable, java);
	}

	public void addNamespaceMapping(String ns, String portable) {
		javaToPortableNamespaceMap.put(ns, portable);
		portableToJavaNamespaceMap.put(portable, ns);
	}

	static {
		for (int i = 0; i < serializers.length; ++i) {
			TypeSerializer s = serializers[i];
			if (s != null) {
				if (s.type.ordinal() != i) {
					throw new AssertionError("Invalid serializers");
				}

				if (s.serializedClass != null) {
					classMap.put(s.serializedClass, s);
				}
				if (s.primitiveClass != null) {
					classMap.put(s.primitiveClass, s);
				}
				if (s.canSerializeIsSpecial()) {
					specialSerializer.add(s);
				}
			}
		}
	}

	public PortableSerializer() {
		// add standard types
		addMapping(Object.class.getName(), "Object");
		addNamespaceMapping("hu.qgears.coolrmi", "coolrmi");
		addNamespaceMapping("hu.qgears.coolrmi.messages", "coolrmi.messages");
	}

	@Override
	public byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serialize(os, o, null);
		return os.toByteArray();
	}

	private final JavaType replaceSerializableType = new JavaType(
			IReplaceSerializable.class);
	TypeSerializer getSerializer(JavaType typ) {
		if (getServiceRegistry().getReplacer(typ.getCls()) != null) {
			typ = replaceSerializableType;
		}

		TypeSerializer s = classMap.get(typ.getCls());
		if (s == null) {
			for (TypeSerializer sc : specialSerializer) {
				if (sc.canSerialize(this, typ)) {
					return sc;
				}
			}

			throw new RuntimeException(
					"Unserializable class " + typ.getCls().getName());
		} else {
			return s;
		}
	}

	TypeSerializer getSerializer(int idx) {
		return serializers[idx];
	}

	void serialize(OutputStream os, Object o, JavaType typ)
			throws IOException {
		TypeSerializer s;
		o = getServiceRegistry().replaceObject(o);

		boolean full = typ == null;
		if (typ == null && o == null) {
			s = serializers[TypeId.Null.ordinal()];
		} else {
			if (typ == null) {
				typ = new JavaType(o);
			}
			s = getSerializer(typ);
			if (s.isPolymorphic()) {
				full = true;
				if (o != null) {
					typ = new JavaType(typ, o);
					s = getSerializer(typ);
				} else {
					s = serializers[TypeId.Null.ordinal()];
				}
			}
		}

		if (full) {
			s.writeType(this, os, typ);
		}
		s.serialize(this, os, o, typ);
	}

	@Override
	public Object deserialize(byte[] bs) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bs);
		return deserialize(is, null);
	}

	Object deserialize(InputStream is, JavaType typ) throws Exception {
		JavaType desType;
		TypeSerializer ser;

		if (typ != null) {
			ser = getSerializer(typ);
			if (ser.isPolymorphic()) {
				ser = getSerializer(is.read());
				desType = ser.readType(this, is);
			} else {
				desType = typ;
			}
		} else {
			ser = getSerializer(is.read());
			desType = ser.readType(this, is);
		}

		Object ret = ser.deserialize(this, is, desType);
		if (ret instanceof IReplaceSerializable) {
			return ((IReplaceSerializable) ret).readResolve();
		}
		return ret;
	}

	String getPortableClassName(JavaType typ) {
		String javaName = typ.getCls().getName();
		String portableName = javaToPortableNameMap.get(javaName);
		if (portableName != null) {
			return portableName;
		}

		String ns = typ.getCls().getPackage().getName();
		portableName = javaToPortableNamespaceMap.get(ns);
		if (portableName != null) {
			portableName = portableName + '.'
					+ javaName.substring(javaName.lastIndexOf('.') + 1);
		} else {
			System.err.println("Warning: no portable name for " + javaName);
			portableName = javaName;
		}

		// cache for future
		addMapping(javaName, portableName);
		return portableName;
	}

	public void writeClassName(OutputStream os, JavaType typ) throws IOException {
		Utils.writeString(os, getPortableClassName(typ));

		for (JavaType x : typ.getGenericTypes()) {
			writeClassName(os, x);
		}
	}


	String getJavaClassName(String portableName) {
		String javaName = portableToJavaNameMap.get(portableName);
		if (javaName != null) {
			return javaName;
		}

		int dotPos = portableName.lastIndexOf('.');
		String ns = portableName.substring(0, dotPos);
		javaName = portableToJavaNamespaceMap.get(ns);
		if (javaName != null) {
			javaName = javaName + '.' + portableName.substring(dotPos + 1);
		} else {
			javaName = portableName;
		}

		// cache for future
		addMapping(javaName, portableName);
		return javaName;
	}

	public JavaType readClassName(InputStream is)
			throws ClassNotFoundException, IOException {
		Class<?> cls = loadClass(Utils.readString(is));

		Type[] generics = cls.getTypeParameters();
		JavaType[] jGenerics = new JavaType[generics.length];
		for (int i = 0; i < generics.length; ++i) {
			jGenerics[i] = readClassName(is);
		}
		return new JavaType(cls, jGenerics);
	}

	private Class<?> loadClass(String name) throws ClassNotFoundException {
		return getClassLoader().loadClass(getJavaClassName(name));
	}
}
