package hu.qgears.coolrmi.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import hu.qgears.coolrmi.serializer.ICustomPortableSerializable;
import hu.qgears.coolrmi.serializer.JavaType;
import hu.qgears.coolrmi.serializer.ObjectSerializer;
import hu.qgears.coolrmi.serializer.PortableSerializer;
import hu.qgears.coolrmi.serializer.Utils;


public class CoolRMICreateProxy
	extends AbstractCoolRMIMessage
	implements Serializable, ICustomPortableSerializable {
	private static final long serialVersionUID = 1L;
	private long proxyId;
	private String ifaceName;
	public String getIfaceName() {
		return ifaceName;
	}

	public long getProxyId() {
		return proxyId;
	}

	public CoolRMICreateProxy() {
		
	}

	public CoolRMICreateProxy(long queryId, 
			long proxyId,
			String ifaceName) {
		super(queryId);
		this.proxyId = proxyId;
		this.ifaceName=ifaceName;
	}

	@Override
	public String toString() {
		return "create proxy "+proxyId+" "+ifaceName;
	}
	@Override
	public String getName() {
		return "Create proxy: "+proxyId+" "+ifaceName;
	}

	@SuppressWarnings("unused") // reflection
	private CoolRMICreateProxy(PortableSerializer serializer, InputStream is)
			throws Exception {
		ObjectSerializer.defaultDeserialize(serializer, is, this,
				AbstractCoolRMIMessage.class);
		proxyId = Utils.read64(is);
		ifaceName = serializer.readClassName(is).getCls().getName();
	}

	@Override
	public void serialize(PortableSerializer serializer, OutputStream os)
			throws IOException {
		ObjectSerializer.defaultSerialize(serializer, os, this,
				AbstractCoolRMIMessage.class);
		Utils.write64(os, proxyId);
		try {
			serializer.writeClassName(os,
					new JavaType(Class.forName(ifaceName)));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
