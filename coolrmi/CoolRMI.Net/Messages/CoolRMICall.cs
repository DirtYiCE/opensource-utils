using System;
using System.Reflection;
using System.Threading.Tasks;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMICall : AbstractCoolRMICall
    {
        public long ProxyId { get; }
        public string Method { get; }
        public object[] Args { get; }
        public bool IsStopOnException { get; }

        public CoolRMICall(long callId, long proxyId, string method,
            object[] args, bool stopOnException) : base(callId)
        {
            ProxyId = proxyId;
            Method = method;
            Args = args;
            IsStopOnException = stopOnException;
        }

        public override string ToString()
        {
            return "CoolRMICall: " + QueryId + " proxy: " + ProxyId + "." +
                   Method;
        }

        public override void ExecuteServerSide(CoolRMIRemoter remoter,
            TaskScheduler scheduler)
        {
            new Task(() =>
            {
                var reply = ExecuteOnExecutorThread(remoter);
                remoter.Send(reply);
            }).Start(scheduler);
        }

        public CoolRMIReply ExecuteOnExecutorThread(CoolRMIRemoter remoter)
        {
            var proxy = remoter.GetProxyById(ProxyId);
            if (proxy == null)
            {
                return new CoolRMIReply(QueryId, null,
                    new CoolRMIException("Server side proxy does not exists"));
            }

            var service = proxy.Service;
            var method = service.GetType().GetMethod(Method);
            if (method != null)
            {
                var args = remoter.ResolveProxyInParametersClientSide(Args);
                try
                {
                    CoolRMIRemoter.CurrentRemoter = remoter;
                    var ret = method.Invoke(service, args);
                    ret = remoter.ResolveProxyInParameterServerSide(ret);
                    return new CoolRMIReply(QueryId, ret, null);
                }
                catch (TargetInvocationException e)
                {
                    return new CoolRMIReply(QueryId, null, e.InnerException);
                }
                catch (Exception e)
                {
                    Console.WriteLine("Err method: "+Method);
                    return new CoolRMIReply(QueryId, null, e);
                }
                finally
                {
                    CoolRMIRemoter.CurrentRemoter = null;
                }
            }

            return new CoolRMIReply(QueryId, null,
                new CoolRMIException("No such method on service: " + service +
                                     " (callid " + QueryId + ") " + Method));
        }
    }
}
