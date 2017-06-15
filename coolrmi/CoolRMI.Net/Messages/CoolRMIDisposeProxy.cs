using System;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIDisposeProxy : AbstractCoolRMIMessage
    {
        [PortableFieldName("proxyId")]
        public long ProxyId { get; }
        public override string Name => ToString();

        public CoolRMIDisposeProxy(long queryId, long proxyId) : base(queryId)
        {
            ProxyId = proxyId;
        }

        public override string ToString()
        {
            return "Dispose proxy " + ProxyId;
        }
    }
}
