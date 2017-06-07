package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExceptionSerializer extends TypeSerializer {
	private static final JavaType throwableType = new JavaType(Throwable.class);

	public ExceptionSerializer() {
		super(TypeId.Exception, Throwable.class, null);
	}

	@Override
	public boolean canSerializeIsSpecial() {
		return true;
	}

	@Override
	public boolean canSerialize(PortableSerializer serializer, JavaType typ) {
		return Throwable.class.isAssignableFrom(typ.getCls());
	}

	/*
	 * Format:
	 *  string exceptionType
	 *  string message
	 *  int stacktraceCount times:
	 *    string class
	 *    string method
	 *    string file
	 *    int line
	 *  Throwable cause
	 */

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		if (o == null) {
			Utils.writeString(os, null);
			return;
		}

		Throwable t = (Throwable) o;
		Utils.writeString(os, serializer.getPortableClassName(t.getClass()));
		Utils.writeString(os, t.getMessage());

		StackTraceElement[] trace = t.getStackTrace();
		Utils.write32(os, trace.length);
		for (int i = 0; i < trace.length; ++i) {
			StackTraceElement item = trace[i];
			Utils.writeString(os, serializer.getPortableClassName(item.getClassName()));
			Utils.writeString(os, item.getMethodName());
			Utils.writeString(os, item.getFileName());
			Utils.write32(os, item.getLineNumber());
		}

		serializer.serialize(os, t.getCause(), throwableType);
	}

	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		String className = Utils.readString(is);
		if (className == null) {
			return null;
		}

		String message = Utils.readString(is);
		int len = Utils.read32(is);

		StackTraceElement[] trace = new StackTraceElement[len];
		for (int i = 0; i < len; ++i) {
			String clsName = serializer.getJavaClassName(Utils.readString(is));
			String method = Utils.readString(is);
			String file = Utils.readString(is);
			int line = Utils.read32(is);

			trace[i] = new StackTraceElement(clsName, method, file, line);
		}

		Throwable cause = (Throwable) serializer.deserialize(is, throwableType);

		Class<?> excClass = serializer.loadClass(className);
		Throwable t = (Throwable) excClass
				.getConstructor(String.class, Throwable.class)
				.newInstance(message, cause);
		t.setStackTrace(trace);

		return t;
	}

}
