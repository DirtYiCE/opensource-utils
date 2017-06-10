using System;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIRequestServiceQuery : AbstractCoolRMIMessage
    {
        public string ServiceName { get; }
        public override string Name => ToString();

        public CoolRMIRequestServiceQuery(long queryId, string serviceName) :
            base(queryId)
        {
            ServiceName = serviceName;
        }

        public override string ToString()
        {
            return "Request service " + ServiceName + " " + QueryId;
        }
    }

    [Serializable]
    public class CoolRMIRequestServiceReply : AbstractCoolRMIReply
    {
        public long ProxyId { get; }
        public string InterfaceName { get; }
        public override string Name => ToString();

        public CoolRMIRequestServiceReply(long queryId, long proxyId,
            string interfaceName) : base(queryId)
        {
            ProxyId = proxyId;
            InterfaceName = interfaceName;
        }

        public override string ToString()
        {
            return "Request service reply";
        }
    }
}
