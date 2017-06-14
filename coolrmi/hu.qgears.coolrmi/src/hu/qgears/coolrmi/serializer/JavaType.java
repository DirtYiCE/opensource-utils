package hu.qgears.coolrmi.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class JavaType {
	private static final JavaType[] EMPTY_LIST = new JavaType[0];
	public static final JavaType OBJECT_TYPE = new JavaType(Object.class);

	private final Class<?> cls;
	private JavaType[] genericTypes;

	public JavaType(Class<?> cls) {
		this.cls = cls;
	}

	public JavaType(Class<?> cls, JavaType[] genericTypes) {
		this.cls = cls;
		this.genericTypes = genericTypes;
	}

	public JavaType(Class<?> cls, Type genericType) {
		this.cls = cls;
		genFromParametrizedType(genericType);
	}

	public JavaType(Type typ) {
		this.cls = (Class<?>) typ;
		genFromParametrizedType(typ);
	}

	public JavaType(Object o) {
		this.cls = o.getClass();
		if (o instanceof GenericClass) {
			this.genericTypes = ((GenericClass) o).getActualTypeArguments();
		}
	}

	public JavaType(JavaType jt, Object o) {
		this.cls = o.getClass();
		if (o instanceof GenericClass) {
			this.genericTypes = ((GenericClass) o).getActualTypeArguments();
		} else {
			this.genericTypes = jt.genericTypes;
		}
	}

	public Class<?> getCls() {
		return cls;
	}
	public JavaType[] getGenericTypes() {
		if (genericTypes == null) {
			genUnknownTypeParams();
		}
		return genericTypes;
	}

	private void genFromParametrizedType(Type genericType) {
		if (genericType instanceof ParameterizedType) {
			Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
			genericTypes = new JavaType[args.length];
			for (int i = 0; i < args.length; ++i) {
				genericTypes[i] = new JavaType(args[i]);
			}
		}
	}

	private void genUnknownTypeParams() {
		TypeVariable<?>[] pars = cls.getTypeParameters();
		if (pars.length == 0) {
			genericTypes = EMPTY_LIST;
		} else {
			System.err.println(
					"Warning: unknown generic params for " + cls.getName());

			genericTypes = new JavaType[pars.length];
			for (int i = 0; i < pars.length; ++i) {
				genericTypes[i] = OBJECT_TYPE;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		appendToString(bld);
		return bld.toString();
	}

	private void appendToString(StringBuilder bld) {
		bld.append(cls.getName());
		JavaType[] generics = getGenericTypes();
		if (generics.length == 0) {
			return;
		}

		bld.append('<');
		for (int i = 0; i < generics.length; ++i) {
			if (i > 0) {
				bld.append(", ");
			}
			generics[i].appendToString(bld);
		}
		bld.append('>');
	}
}
