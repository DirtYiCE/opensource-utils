package hu.qgears.coolrmi.messages;

import java.io.Serializable;

import hu.qgears.coolrmi.serializer.PortableSerializable;


public class CoolRMIDisposeProxy
	extends AbstractCoolRMIMessage
	implements Serializable, PortableSerializable {
	private static final long serialVersionUID = 1L;
	private long proxyId;
	public long getProxyId() {
		return proxyId;
	}

	public CoolRMIDisposeProxy() {
		
	}

	public CoolRMIDisposeProxy(long queryId, 
			long proxyId) {
		super(queryId);
		this.proxyId = proxyId;
	}

	@Override
	public String toString() {
		return "Dispose proxy "+proxyId;
	}
	@Override
	public String getName() {
		return toString();
	}
}
