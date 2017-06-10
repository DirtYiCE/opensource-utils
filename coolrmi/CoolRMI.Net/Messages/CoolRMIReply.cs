using System;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIReply : AbstractCoolRMIMethodCallReply
    {
        private object ret;
        public override string Name => ToString();
        public override Exception Exception { get; }
        public override object Ret => ret;

        public CoolRMIReply(long callId, object ret,
            Exception exception) : base(callId)
        {
            this.ret = ret;
            Exception = exception;
        }

        public override string ToString()
        {
            return "CoolRMIReply: " + QueryId;
        }

        public override void EvaluateOnClientSide(CoolRMIProxy proxy, bool returnLast)
        {
            ResolveArgumentsOnClient(proxy.Remoter);
        }

        public void ResolveArgumentsOnClient(CoolRMIRemoter remoter)
        {
            if (Exception == null)
            {
                ret = remoter.ResolveProxyInParameterClientSide(ret);
            }
            // TODO: exception stacktrace mashol
        }
    }
}
