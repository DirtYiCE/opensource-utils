package hu.qgears.coolrmi.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JavaType {
	private static final Type[] EMPTY_LIST = new Type[0];

	private Class<?> cls;
	private Type[] genericTypes = EMPTY_LIST;

	public JavaType(Class<?> cls) {
		this.cls = cls;
	}

	public JavaType(Class<?> cls, Type[] genericTypes) {
		this.cls = cls;
		this.genericTypes = genericTypes;
	}

	public JavaType(Class<?> cls, Type genericType) {
		this.cls = cls;
		if (genericType instanceof ParameterizedType) {
			this.genericTypes = ((ParameterizedType) genericType).getActualTypeArguments();
		}
	}

	public JavaType(Type typ) {
		this.cls = (Class<?>) typ;
		if (typ instanceof ParameterizedType) {
			this.genericTypes = ((ParameterizedType) typ).getActualTypeArguments();
		}
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
	public Type[] getGenericTypes() {
		return genericTypes;
	}

	@Override
	public String toString() {
		StringBuilder bld = new StringBuilder();
		bld.append(cls.getName()).append('<');
		for (int i = 0; i < genericTypes.length; ++i) {
			if (i > 0) {
				bld.append(", ");
			}
			bld.append(genericTypes[i].getTypeName());
		}
		return bld.append('>').toString();
	}
}
