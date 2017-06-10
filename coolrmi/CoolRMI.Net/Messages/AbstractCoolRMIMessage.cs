using System;
using System.Threading.Tasks;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public abstract class AbstractCoolRMIMessage
    {
        public long QueryId { get; }
        public abstract string Name { get; }

        protected AbstractCoolRMIMessage() {}
        protected AbstractCoolRMIMessage(long queryId)
        {
            QueryId = queryId;
        }

        /// <summary>
        /// Callback when the last piece of this message has been sent through the (TCP) channel.
        /// Default implementation does nothing.
        /// </summary>
        public virtual void Sent() {}
    }

    [Serializable]
    public abstract class AbstractCoolRMICall : AbstractCoolRMIMessage
    {
        protected AbstractCoolRMICall(long queryId) : base(queryId) {}

        public abstract void ExecuteServerSide(CoolRMIRemoter remoter,
            TaskScheduler scheduler);

        public override string Name => "RMI call";
    }

    [Serializable]
    public abstract class AbstractCoolRMIReply : AbstractCoolRMIMessage
    {
        protected AbstractCoolRMIReply(long queryId) : base(queryId) {}
    }

    [Serializable]
    public abstract class AbstractCoolRMIMethodCallReply : AbstractCoolRMIReply
    {
        protected AbstractCoolRMIMethodCallReply(long queryId) : base(queryId) {}

        /// <summary>
        /// This method must be called to process the method call reply:
        ///
        ///  * in case of method call list call the callbacks of method call replies.
        ///  * process proxied objects to transform them to their client side representation.
        ///  * process returned stack traces: add the server side stack trace to them.
        /// </summary>
        /// <param name="proxy"></param>
        /// <param name="returnLast">when true then return the value returned by last method call.</param>
        public abstract void EvaluateOnClientSide(CoolRMIProxy proxy, bool returnLast);
        /// <summary>
        /// The exception thrown on the server when executing the method.
        /// </summary>
        public abstract Exception Exception { get; }
        /// <summary>
        /// The return value of the method execution on the server. Only valid in case the exception is null.
        /// </summary>
        public abstract object Ret { get; }
    }
}
