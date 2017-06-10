using System;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIProxyPlaceHolder
    {
        public long ProxyId { get; }

        public CoolRMIProxyPlaceHolder(long proxyId)
        {
            ProxyId = proxyId;
        }
    }
}
