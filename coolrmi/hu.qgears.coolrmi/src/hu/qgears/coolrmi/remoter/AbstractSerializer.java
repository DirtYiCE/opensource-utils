package hu.qgears.coolrmi.remoter;

import java.io.IOException;

public abstract class AbstractSerializer {
	private ClassLoader classLoader;
	private CoolRMIServiceRegistry serviceReg;

	/**
	 *
	 * @param serviceReg used to look for object replaces.
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public abstract byte[] serialize(Object o) throws IOException;

	public abstract Object deserialize(byte[] bs) throws Exception;



	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public CoolRMIServiceRegistry getServiceRegistry() {
		return serviceReg;
	}

	public void setServiceRegistry(CoolRMIServiceRegistry serviceReg) {
		this.serviceReg = serviceReg;
	}
}
