package hu.qgears.coolrmi.remoter;

import java.io.IOException;

public interface ISerializer {
	/**
	 *
	 * @param serviceReg used to look for object replaces.
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public byte[] serialize(CoolRMIServiceRegistry serviceReg, Object o)
			throws IOException;

	public Object deserialize(byte[] bs, ClassLoader classLoader)
			throws Exception;
}
