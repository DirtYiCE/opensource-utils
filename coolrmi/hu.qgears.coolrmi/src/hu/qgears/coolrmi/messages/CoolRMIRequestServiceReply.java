package hu.qgears.coolrmi.messages;

import java.io.Serializable;

import hu.qgears.coolrmi.serializer.PortableSerializable;


public class CoolRMIRequestServiceReply extends AbstractCoolRMIReply
		implements Serializable, PortableSerializable {
	private static final long serialVersionUID = 1L;
	private long proxyId;
	private String interfaceName;

	public String getInterfaceName() {
		return interfaceName;
	}
	public CoolRMIRequestServiceReply()
	{
	}
	public CoolRMIRequestServiceReply(long queryId, long proxyId, String interfaceName) {
		super(queryId);
		this.proxyId = proxyId;
		this.interfaceName=interfaceName;
	}

	public long getProxyId() {
		return proxyId;
	}
	@Override
	public String getName() {
		return "Request service reply";
	}
}
