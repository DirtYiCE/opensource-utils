using System;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIRequestServiceQuery : AbstractCoolRMIMessage
    {
        [PortableFieldName("serviceName")]
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
        [PortableFieldName("proxyId")]
        public long ProxyId { get; }
        [PortableFieldName("interfaceName")]
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
