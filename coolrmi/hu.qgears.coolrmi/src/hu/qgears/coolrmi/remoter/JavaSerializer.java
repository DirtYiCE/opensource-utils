package hu.qgears.coolrmi.remoter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import hu.qgears.coolrmi.CoolRMIObjectInputStream;
import hu.qgears.coolrmi.CoolRMIObjectOutputStream;



public class JavaSerializer extends AbstractSerializer {
	@Override
	public byte[] serialize(Object o) throws IOException
	{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		CoolRMIObjectOutputStream oos = new CoolRMIObjectOutputStream(
				getServiceRegistry(), bos);
		oos.writeObject(o);
		oos.close();
		return bos.toByteArray();
	}
	@Override
	public Object deserialize(byte[] bs) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis=new ByteArrayInputStream(bs);
		CoolRMIObjectInputStream ois = new CoolRMIObjectInputStream(
				getClassLoader(), bis);
		try {
			return ois.readObject();
		} finally{
			ois.close();
		}
	}
}
