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

import hu.qgears.coolrmi.remoter.AbstractSerializer;
import hu.qgears.coolrmi.remoter.CoolRMIServiceRegistry;
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
	public byte[] serialize(Object o) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serialize(os, o, null);
		return os.toByteArray();
	}

	TypeSerializer getSerializer(Class<?> cls) {
		if (getServiceRegistry().getReplacer(cls) != null) {
			cls = IReplaceSerializable.class;
		}

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

	void serialize(OutputStream os, Object o, Class<?> cls) throws IOException {
		TypeSerializer s;
		o = getServiceRegistry().replaceObject(o);

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
	public Object deserialize(byte[] bs) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bs);
		return deserialize(is, null);
	}

	Object deserialize(InputStream is, Class<?> cls) throws Exception {
		Class<?> desClass;
		TypeSerializer ser;

		if (cls != null) {
			ser = getSerializer(cls);
			desClass = cls;
		} else {
			ser = getSerializer(is.read());
			desClass = ser.readType(this, is);
		}

		Object ret = ser.deserialize(this, is, desClass);
		if (ret instanceof IReplaceSerializable) {
			return ((IReplaceSerializable) ret).readResolve();
		}
		return ret;
	}

	// TODO remove
	static final ThrowableReplacer throwableReplacer = new ThrowableReplacer();
	@Override
	public void setServiceRegistry(CoolRMIServiceRegistry serviceReg) {
		super.setServiceRegistry(serviceReg);
		serviceReg.addReplaceType(throwableReplacer);
	}
}
