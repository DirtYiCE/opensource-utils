package hu.qgears.coolrmi.messages;

import java.io.Serializable;

import hu.qgears.coolrmi.serializer.PortableSerializable;

abstract public class AbstractCoolRMIReply extends AbstractCoolRMIMessage
		implements Serializable, PortableSerializable {
	private static final long serialVersionUID = 1L;
	public AbstractCoolRMIReply()
	{
		
	}
	public AbstractCoolRMIReply(long queryId) {
		super(queryId);
	}
}
