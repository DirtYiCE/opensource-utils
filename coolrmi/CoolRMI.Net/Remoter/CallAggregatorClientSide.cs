using System;
using CoolRMI.Net.Messages;

namespace CoolRMI.Net.Remoter
{
    public class CallAggregatorClientSide
    {
        private readonly CoolRMIProxy owner;

        public CallAggregatorClientSide(CoolRMIProxy owner)
        {
            this.owner = owner;
        }

        public AbstractCoolRMICall CreateCall(string methodName, object[] args)
        {
            long callId = owner.Remoter.GetNextCallId();
            return new CoolRMICall(callId, owner.Id, methodName, args, false);
        }

        public AbstractCoolRMICall Flush()
        {
            return null;
        }

        public void MethodCallReplied(CoolRMIReply rep)
        {
            if (rep.Exception != null)
            {
                HandleException(rep, rep.Exception);
            }
        }

        protected void HandleException(CoolRMIReply rep, Exception e)
        {
            Console.WriteLine(e);
        }
    }
}
