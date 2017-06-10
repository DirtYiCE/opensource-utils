using System;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Remoting.Proxies;
using CoolRMI.Net.Messages;

namespace CoolRMI.Net.Remoter
{
    public class CoolRMIProxy : RealProxy, IRemotingTypeInfo, IDisposable
    {
        public long Id { get; }
        public CoolRMIRemoter Remoter { get; }
        public bool IsDisposed { get; private set; }
        public ICoolRMIProxy ProxyObject =>
            (ICoolRMIProxy) GetTransparentProxy();
        public CallAggregatorClientSide CallAggregator { get; set; }

        public CoolRMIProxy(CoolRMIRemoter remoter, long id, Type ifaceType)
            : base(ifaceType)
        {
            CallAggregator = new CallAggregatorClientSide(this);
            Remoter = remoter;
            Id = id;
        }

        public void Dispose()
        {
            Remoter.Remove(this);
            IsDisposed = true;
        }

        public override IMessage Invoke(IMessage msg)
        {
            var imsg = (IMethodCallMessage) msg;
            object ret = null;
            switch (imsg.MethodName)
            {
                case "Dispose":
                    Dispose();
                    break;
                case "get_IsProxyDisposed":
                    ret = IsDisposed;
                    break;
                case "get_ProxyHome":
                    ret = Remoter;
                    break;
                case "get_ProxyObject":
                    ret = this;
                    break;
                default:
                    return HandleProxyInvoke(imsg);
            }
            return new ReturnMessage(ret, null, 0, imsg.LogicalCallContext,
                imsg);
        }

        private IMessage HandleProxyInvoke(IMethodCallMessage imsg)
        {
            if (IsDisposed)
            {
                return new ReturnMessage(
                    new CoolRMIException("Proxy is already disposed"), imsg);
            }
            try
            {
                var args = Remoter.ResolveProxyInParametersServerSide(imsg.Args);
                var call = CallAggregator.CreateCall(imsg.MethodName, args);
                if (call == null)
                {
                    return new ReturnMessage(null, null, 0,
                        imsg.LogicalCallContext, imsg);
                }

                var replyFuture = Remoter.GetAbstractReply(call.QueryId);
                Remoter.Send(call);
                var reply =
                    (AbstractCoolRMIMethodCallReply) replyFuture
                        .WaitReply();
                reply.EvaluateOnClientSide(this, true);

                if (reply.Exception != null)
                    return new ReturnMessage(reply.Exception, imsg);
                return new ReturnMessage(reply.Ret, null, 0,
                    imsg.LogicalCallContext, imsg);
            }
            catch (Exception e)
            {
                return new ReturnMessage(
                    new CoolRMIException("Exception doing RMI", e), imsg);
            }
        }

        /// <summary>
        /// Force aggregated method calls to be sent to the server in case all
        /// of them are void calls and thus aggregated.
        /// See CallAggregatorClientSideCompress.
        /// </summary>
        public void FlushAggregated()
        {
            var call = CallAggregator.Flush();
            if (call == null) return;

            var replyFut = Remoter.GetAbstractReply(call.QueryId);
            Remoter.Send(call);
            var reply = (AbstractCoolRMIMethodCallReply) replyFut.WaitReply();
            reply.EvaluateOnClientSide(this, false);
        }

        // IRremotingTypeInfo -- allow cast to ICoolRMIProxy and the proxy type
        public bool CanCastTo(Type fromType, object o)
        {
            return fromType == typeof(ICoolRMIProxy) ||
                   fromType == GetProxiedType();
        }

        public string TypeName { get; set; }
    }
}
