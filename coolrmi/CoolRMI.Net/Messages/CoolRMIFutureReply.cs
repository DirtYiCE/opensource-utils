using System;
using System.Threading.Tasks;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Messages
{
    public class CoolRMIFutureReply
    {
        private readonly CoolRMIRemoter remoter;
        private readonly TaskCompletionSource<AbstractCoolRMIReply> src =
            new TaskCompletionSource<AbstractCoolRMIReply>();
        public long CallId { get; }

        public CoolRMIFutureReply(CoolRMIRemoter remoter, long callId)
        {
            this.remoter = remoter;
            CallId = callId;
        }

        public void Received(AbstractCoolRMIReply reply)
        {
            src.SetResult(reply);
        }

        public AbstractCoolRMIReply WaitReply()
        {
            var task = src.Task;
            if (!task.Wait(remoter.TimeoutMillis))
                throw new CoolRMITimeoutException("Timeout executing call: " + CallId);
            remoter.RemoveAwaitingReply(this);
            if (task.IsCanceled) throw new OperationCanceledException();
            return task.Result;
        }

        public void Cancelled()
        {
            src.SetCanceled();
        }
    }
}
