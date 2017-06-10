using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using CoolRMI.Net.Messages;
using CoolRMI.Net.Multiplexer;
using CoolRMI.Net.Streams;

namespace CoolRMI.Net.Remoter
{
    public class CoolRMIRemoter : IDisposable
    {
        private class SocketMultiplexerListener : ISocketMultiplexerListener
        {
            private readonly CoolRMIRemoter thiz;

            public SocketMultiplexerListener(CoolRMIRemoter thiz)
            {
                this.thiz = thiz;
            }

            public void MessageReceived(byte[] bs)
            {
                thiz.MessageReceived(bs);
            }

            public void PipeBroken(Exception e)
            {
                thiz.PipeBroken(e);
            }
        }

        private AbstractSerializer serializer;
        private CoolRMIServiceRegistry serviceRegistry =
            new CoolRMIServiceRegistry();
        private SocketMultiplexer multiplexer;
        private IConnection sock;
        public bool IsConnected { get; private set; }
        public bool IsClosed { get; private set; }
        private readonly ConcurrentDictionary<long, CoolRMIFutureReply> replies =
            new ConcurrentDictionary<long, CoolRMIFutureReply>();
        private readonly IDictionary<long, CoolRMIProxy> proxies =
            new ConcurrentDictionary<long, CoolRMIProxy>();
        private readonly ConcurrentDictionary<long, CoolRMIServerSideObject> services =
            new ConcurrentDictionary<long, CoolRMIServerSideObject>();
        private readonly TaskScheduler scheduler;

        private static readonly ThreadLocal<CoolRMIRemoter> currentRemoter =
            new ThreadLocal<CoolRMIRemoter>();

        private long callCounter;
        private long proxyCounter;

        public CoolRMIServiceRegistry ServiceRegistry
        {
            get { return serviceRegistry; }
            set
            {
                serviceRegistry = value;
                Serializer.ServiceRegistry = value;
            }
        }

        public AbstractSerializer Serializer
        {
            get { return serializer; }
            set
            {
                serializer = value;
                value.ServiceRegistry = ServiceRegistry;
            }
        }

        public static CoolRMIRemoter CurrentRemoter
        {
            get { return currentRemoter.Value; }
            set { currentRemoter.Value = value; }
        }

        public int TimeoutMillis { get; set; } = 30000;

        public CoolRMIRemoter(AbstractSerializer serializer, bool guaranteeOrdering)
        {
            Serializer = serializer;
            scheduler = guaranteeOrdering
                ? new SingleThreadTaskScheduler()
                : TaskScheduler.Default;
        }

        internal void Connect(IConnection conn)
        {
            sock = conn;
            multiplexer = new SocketMultiplexer(sock.GetStream(),
                new SocketMultiplexerListener(this));
            IsConnected = true;
            multiplexer.Start();
        }

        internal void Remove(CoolRMIProxy proxy)
        {
            proxies.Remove(proxy.Id);
            var message = new CoolRMIDisposeProxy(GetNextCallId(), proxy.Id);
            Send(message);
        }

        public void Send(AbstractCoolRMIMessage message)
        {
            var bs = serializer.Serialize(message);
            multiplexer.AddMessageToSend(bs, message);
        }

        public long GetNextCallId()
        {
            return Interlocked.Increment(ref callCounter) - 1; // TODO kell?
        }

        public long GetNextProxyId()
        {
            return Interlocked.Increment(ref proxyCounter) - 1;
        }

        public void MessageReceived(byte[] msg)
        {
            try
            {
                var message = serializer.Deserialize(msg);
                if (message is AbstractCoolRMICall)
                    DoCall((AbstractCoolRMICall) message);
                else if (message is CoolRMIClose)
                    Dispose();
                else if (message is CoolRMIRequestServiceQuery)
                    HandleRequestServiceQuery(
                        (CoolRMIRequestServiceQuery) message);
                else if (message is AbstractCoolRMIReply)
                    HandleReply((AbstractCoolRMIReply) message);
                else if (message is CoolRMIDisposeProxy)
                    HandleDisposeProxy((CoolRMIDisposeProxy) message);
                else if (message is CoolRMICreateProxy)
                    HandleCreateProxy((CoolRMICreateProxy) message);
                else if (message is CoolRMIDisconnect)
                    Dispose();
                else
                    throw new InvalidDataException(
                        "Unhandled message type: " + message);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        }

        private void HandleCreateProxy(CoolRMICreateProxy message)
        {
            var ifaceType = Type.GetType(message.IfaceName);
            var proxy = new CoolRMIProxy(this, message.ProxyId, ifaceType);
            proxies.Add(proxy.Id, proxy);
            Send(new CoolRMICreateProxyReply(message.QueryId));
        }

        private void HandleDisposeProxy(CoolRMIDisposeProxy message)
        {
            CoolRMIServerSideObject service;
            if (services.TryRemove(message.ProxyId, out service))
                service.Dispose();
        }

        private void HandleRequestServiceQuery(
            CoolRMIRequestServiceQuery message)
        {
            var service = serviceRegistry.GetService(message.ServiceName);
            if (service == null)
            {
                Send(new CoolRMIRequestServiceReply(message.QueryId, -1, null));
            }
            else
            {
                var sso = CreateProxyObject(service);
                Send(new CoolRMIRequestServiceReply(message.QueryId,
                    sso.ProxyId, sso.Iface.AssemblyQualifiedName));
            }
        }

        private void HandleReply(AbstractCoolRMIReply reply)
        {
            CoolRMIFutureReply future;
            if (replies.TryRemove(reply.QueryId, out future))
            {
                future.Received(reply);
            }
            else
            {
                throw new InvalidOperationException(
                    "Reply received but noone waits for it: " + reply + " " +
                    reply.QueryId);
            }
        }

        private void DoCall(AbstractCoolRMICall abstractCall)
        {
            abstractCall.ExecuteServerSide(this, scheduler);
        }

        internal object[] ResolveProxyInParametersServerSide(object[] args)
        {
            if (args == null) return null;
            for (int i = 0; i < args.Length; ++i)
            {
                args[i] = ResolveProxyInParameterServerSide(args[i]);
            }
            return args;
        }

        internal object[] ResolveProxyInParametersClientSide(object[] args)
        {
            if (args == null) return null;
            for (int i = 0; i < args.Length; ++i)
            {
                args[i] = ResolveProxyInParameterClientSide(args[i]);
            }
            return args;
        }

        internal object ResolveProxyInParameterServerSide(object arg)
        {
            if (arg != null)
            {
                var iftype = ServiceRegistry.GetProxyType(arg.GetType());
                if (iftype != null)
                {
                    var ssop = CreateServerSideProxyObject(
                        new CoolRMIShareableObject(iftype, arg));
                    var sso = ssop.ServerSideProxyObject;
                    return new CoolRMIProxyPlaceHolder(sso.ProxyId);
                }
            }

            var proxy = arg as ICoolRMIServerSideProxy;
            if (proxy == null) return arg;
            {
                var sso = proxy.ServerSideProxyObject;
                return new CoolRMIProxyPlaceHolder(sso.ProxyId);
            }
        }

        internal object ResolveProxyInParameterClientSide(object arg)
        {
            var ph = arg as CoolRMIProxyPlaceHolder;
            if (ph == null) return arg;

            CoolRMIProxy proxy;
            return proxies.TryGetValue(ph.ProxyId, out proxy)
                ? proxy.ProxyObject
                : null;
        }

        public void PipeBroken(Exception e)
        {
            // If socket is not closed by query then the exception is logged.
            if (!IsClosed) Console.WriteLine(e);
            Dispose();
        }

        public virtual void Dispose()
        {
            IsConnected = false;
            IsClosed = true;
            multiplexer.Stop();
            sock.Dispose();
            (scheduler as IDisposable)?.Dispose();

            foreach (var x in replies.Keys.ToArray())
            {
                CoolRMIFutureReply tmp;
                if (replies.TryRemove(x, out tmp)) tmp.Cancelled();
            }
        }

        /// <summary>
        /// Create future reply object.
	    /// Must be called before sending the query!
        /// </summary>
        /// <param name="callId"></param>
        /// <returns>the reply object</returns>
        internal CoolRMIFutureReply GetAbstractReply(long callId)
        {
            var ret = new CoolRMIFutureReply(this, callId);
            ((IDictionary<long, CoolRMIFutureReply>) replies).Add(callId, ret);
            return ret;
        }

        /// <summary>
        /// Create a client proxy of the specified service. The method will not
        /// connect to the server. <br/>
        /// The generated proxy object will connect and disconnect to the server on
        /// each query (method call). Invalid service name or incompatible interface
        /// problems will only be reported when using the interface.
        ///
        /// User exceptions are passed from the server if occur. Communication
        /// related problems are thrown as CoolRMIException.
        /// </summary>
        /// <param name="serviceName">The service name.</param>
        /// <typeparam name="TIface">
        ///   The communication interface. Must be compatible with the one
        ///   deployed on the server.
        /// </typeparam>
        /// <returns>
        ///   The client proxy to the given service. Will implement the passed
        ///   interface.
        /// </returns>
        public TIface GetService<TIface>(string serviceName)
        {
            var query = new CoolRMIRequestServiceQuery(GetNextCallId(), serviceName);
            var replyFuture=GetAbstractReply(query.QueryId);
            Send(query);
            var reply = (CoolRMIRequestServiceReply) replyFuture.WaitReply();
            var proxy = new CoolRMIProxy(this, reply.ProxyId, typeof(TIface));
            proxies.Add(proxy.Id, proxy);

            return (TIface) proxy.ProxyObject;
        }

        private CoolRMIServerSideObject CreateProxyObject(CoolRMIShareableObject service)
        {
            var ret = new CoolRMIServerSideObject(GetNextProxyId(),
                service.Interface, service.Service);
            ((IDictionary<long, CoolRMIServerSideObject>) services).Add(
                ret.ProxyId, ret);
            return ret;
        }

        private ICoolRMIServerSideProxy CreateServerSideProxyObject(
            CoolRMIShareableObject service)
        {
            var sso = CreateProxyObject(service);
            var ret = new CoolRMIServerSideProxy(this, sso);
            var req = new CoolRMICreateProxy(GetNextCallId(), sso.ProxyId,
                sso.Iface.AssemblyQualifiedName);
            var replyFut = GetAbstractReply(req.QueryId);
            Send(req);
            replyFut.WaitReply();
            return ret.ProxyObject;
        }

        public CoolRMIServerSideObject GetProxyById(long proxyId)
        {
            return services[proxyId];
        }

        public void RemoveAwaitingReply(CoolRMIFutureReply reply)
        {
            ((IDictionary<long, CoolRMIFutureReply>) replies).Remove(reply
                .CallId);
        }
    }
}
