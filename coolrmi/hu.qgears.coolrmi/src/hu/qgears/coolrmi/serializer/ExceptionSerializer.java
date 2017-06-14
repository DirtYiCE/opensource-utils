package hu.qgears.coolrmi.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@Override
	public boolean isPolymorphic() {
		return true;
	}

	@Override
	public void writeType(PortableSerializer serializer, OutputStream os,
			JavaType typ) throws IOException {
		super.writeType(serializer, os, typ);
		serializer.writeClassName(os, typ);
	}

	@Override
	public JavaType readType(PortableSerializer serializer, InputStream is)
			throws IOException, ClassNotFoundException {
		return serializer.readClassName(is);
	}


	private static Map<StackTraceElement, String> origStackTraceLines =
			Collections.synchronizedMap(new WeakHashMap<StackTraceElement, String>());


	/*
	 * Format:
	 *  string message
	 *  string trace
	 *  Throwable cause
	 */

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os,
			Object o, JavaType typ) throws IOException {
		Throwable t = (Throwable) o;
		Utils.writeString(os, t.getMessage());

		StringBuilder bd = new StringBuilder();
		StackTraceElement[] trace = t.getStackTrace();
		for (int i = 0; i < trace.length; ++i) {
			StackTraceElement item = trace[i];
			String orig = origStackTraceLines.get(item);
			if (orig != null) {
				bd.append(orig).append('\n');
			} else {
				bd.append("\tat ").append(item.toString()).append('\n');
			}
		}

		Utils.writeString(os, bd.toString());
		serializer.serialize(os, t.getCause(), throwableType);
	}

	// java formats:
	// "\tat package.class.method(file:line)"
	// "\tat package.class.method(file)" (=> line <0)
	// "\tat package.class.method(Native Method)" (=> line = -2)
	// "\tat package.class.method(Unknown Source)" (=> file == null)
	private static final Pattern JAVA_TRACE = Pattern.compile("^\tat (.*)\\.([^.]*)\\((.*?)(?::(\\d+))?\\)$");

	// dotnet: msdn example "   at Namespace.Class.Method(String args)"
	private static final Pattern DOTNET_TRACE = Pattern.compile("^ *at (.*)\\.([^.]*)\\((.*)\\)");
	// mono: "  at Namespace.Class.Method[Generic] (Args) [0x12345] in file:line"
	//       "  at (wrapper managed-to-native) System.Object:__icall_wrapper_mono_remoting_wrapper (intptr,intptr)"


	@Override
	public Object deserialize(PortableSerializer serializer, InputStream is,
			JavaType typ) throws Exception {
		String message = Utils.readString(is);
		String strTrace = Utils.readString(is);
		String[] traceItems = strTrace.split("\n");

		StackTraceElement[] trace = new StackTraceElement[traceItems.length];
		for (int i = 0; i < trace.length; ++i) {
			trace[i] = parseStackTraceLine(traceItems[i]);
		}

		Throwable cause = (Throwable) serializer.deserialize(is, throwableType);

		Throwable t = (Throwable) typ.getCls()
				.getConstructor(String.class, Throwable.class)
				.newInstance(message, cause);
		t.setStackTrace(trace);

		return t;
	}

	private static StackTraceElement parseStackTraceLine(String item) {
		String cls, method, file = null;
		int line = -1;

		Matcher m = JAVA_TRACE.matcher(item);
		if (m.matches()) {
			cls = m.group(1);
			method = m.group(2);
			file = m.group(3);
			if (file.equals("Native Method")) {
				file = null;
				line = -2;
			} else if (file.equals("Unknown Source")) {
				file = null;
			} else if (m.group(4) != null) {
				line = Integer.parseInt(m.group(4));
			}
		} else {
			m = DOTNET_TRACE.matcher(item);
			if (m.matches()) {
				cls = m.group(1);
				method = m.group(2);
				if (m.group(3) != null) {
					file = m.group(3);
					line = Integer.parseInt(m.group(4));
				}
			} else {
				cls = item;
				method = ""; // no better idea
			}
		}

		StackTraceElement ret = new StackTraceElement(cls, method, file, line);
		origStackTraceLines.put(ret, item);
		return ret;
	}

}
