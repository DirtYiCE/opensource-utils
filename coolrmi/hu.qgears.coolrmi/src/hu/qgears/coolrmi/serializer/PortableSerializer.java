package hu.qgears.coolrmi.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
import hu.qgears.coolrmi.remoter.ISerializer;

public class PortableSerializer implements ISerializer {
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
	};

	private static final Map<Class<?>, TypeSerializer> classMap =
			new HashMap<Class<?>, TypeSerializer>();
	private static final List<TypeSerializer> specialSerializer =
			new ArrayList<TypeSerializer>();

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

	@Override
	public byte[] serialize(CoolRMIServiceRegistry serviceReg, Object o)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			serialize(os, o, null);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	TypeSerializer getSerializer(Class<?> cls) {
		TypeSerializer s = classMap.get(cls);
		if (s == null) {
			for (TypeSerializer sc : specialSerializer) {
				if (sc.canSerialize(this, cls)) {
					return sc;
				}
			}

			throw new RuntimeException("Unserializable class " + cls.getName());
		} else {
			return s;
		}
	}

	TypeSerializer getSerializer(int idx) {
		return serializers[idx];
	}

	Class<?> getClassForSerialization(Class<?> cls) {
		TypeSerializer ser = getSerializer(cls);
		return ser.isPolymorphic() ? null : cls;
	}

	void serialize(OutputStream os, Object o, Class<?> cls) throws Exception {
		TypeSerializer s;

		boolean full = cls == null;
		if (cls == null && o == null) {
			s = serializers[Type.Null.ordinal()];
		} else {
			if (cls == null) {
				cls = o.getClass();
			}
			s = getSerializer(cls);
		}

		if (full) {
			s.writeType(this, os, cls);
		}
		s.serialize(this, o, os);
	}

	@Override
	public Object deserialize(byte[] bs, ClassLoader classLoader)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream is = new ByteArrayInputStream(bs);
		try {
			return deserialize(is, classLoader, null);
		} catch (IOException e) {
			throw e;
		} catch (ClassNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	Object deserialize(InputStream is, ClassLoader classLoader,
			Class<?> cls) throws Exception {
		Class<?> desClass;
		TypeSerializer ser;

		if (cls != null) {
			ser = getSerializer(cls);
			desClass = cls;
		} else {
			ser = getSerializer(is.read());
			desClass = ser.readType(this, is, classLoader);
		}

		return ser.deserialize(this, is, classLoader, desClass);
	}

}
