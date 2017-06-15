using System;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    [PortableSerializable]
    public class CoolRMIProxyPlaceHolder
    {
        [PortableFieldName("proxyId")]
        public long ProxyId { get; }

        public CoolRMIProxyPlaceHolder(long proxyId)
        {
            ProxyId = proxyId;
        }
    }
}
