package hu.qgears.coolrmi.messages;

import java.io.Serializable;

import hu.qgears.coolrmi.serializer.PortableSerializable;


public class CoolRMIRequestServiceQuery
	extends AbstractCoolRMIMessage
	implements Serializable, PortableSerializable {
	private static final long serialVersionUID = 1L;
	private String serviceName;
	public CoolRMIRequestServiceQuery() {
		
	}

	public CoolRMIRequestServiceQuery(long queryId, String serviceName) {
		super(queryId);
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}
	@Override
	public String toString() {
		return "Request service "+serviceName+" "+getQueryId();
	}

	@Override
	public String getName() {
		return toString();
	}
}
