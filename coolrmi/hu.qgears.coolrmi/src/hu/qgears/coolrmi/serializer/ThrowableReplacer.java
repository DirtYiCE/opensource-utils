package hu.qgears.coolrmi.serializer;

import hu.qgears.coolrmi.remoter.CoolRMIReplaceEntry;
import hu.qgears.coolrmi.remoter.IReplaceSerializable;

public class ThrowableReplacer extends CoolRMIReplaceEntry {
	public static class MyStackTraceElement implements PortableSerializable {
		public String declaringClass;
		public String methodName;
		public String fileName;
		public int    lineNumber;

		public MyStackTraceElement(StackTraceElement el) {
			declaringClass = el.getClassName();
			methodName = el.getMethodName();
			fileName = el.getFileName();
			lineNumber = el.getLineNumber();
		}

		public StackTraceElement toJavaElement() {
			return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
		}
	};

	public static class SimpleThrowable implements IReplaceSerializable {
		private static final long serialVersionUID = 1L;
		private String message;
		private MyStackTraceElement[] trace;

		public SimpleThrowable(String message, StackTraceElement[] stackTraceElements) {
			this.message = message;
			trace = new MyStackTraceElement[stackTraceElements.length];
			for (int i = 0; i < stackTraceElements.length; ++i) {
				trace[i] = new MyStackTraceElement(stackTraceElements[i]);
			}
		}

		@Override
		public Object readResolve() {
			RuntimeException ex = new RuntimeException(message);
			StackTraceElement[] stackTrace = new StackTraceElement[trace.length];
			for (int i = 0; i < trace.length; ++i) {
				stackTrace[i] = trace[i].toJavaElement();
			}
			ex.setStackTrace(stackTrace);
			return ex;
		}

	}

	public ThrowableReplacer() {
		super(Throwable.class);
	}

	@Override
	public IReplaceSerializable doReplace(Object o) {
		Throwable t = (Throwable) o;
		return new SimpleThrowable(t.toString(), t.getStackTrace());
	}
}
