using System;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMICreateProxy : AbstractCoolRMIMessage
    {
        public long ProxyId { get; }
        public string IfaceName { get; }
        public override string Name => ToString();

        public CoolRMICreateProxy(long queryId, long proxyId,
            string ifaceName) : base(queryId)
        {
            ProxyId = proxyId;
            IfaceName = ifaceName;
        }

        public override string ToString()
        {
            return "Create proxy: " + ProxyId + " " + IfaceName;
        }
    }

    [Serializable]
    public class CoolRMICreateProxyReply : AbstractCoolRMIReply
    {
        public override string Name => ToString();

        public CoolRMICreateProxyReply(long queryId) : base(queryId) {}

        public override string ToString()
        {
            return "proxy created";
        }
    }
}
