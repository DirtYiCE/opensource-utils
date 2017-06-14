using System;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    [PortableSerializable]
    public class CoolRMIProxyPlaceHolder
    {
        public long ProxyId { get; }

        public CoolRMIProxyPlaceHolder(long proxyId)
        {
            ProxyId = proxyId;
        }
    }
}
