using System;
using System.Net;
using System.Threading;
using CoolRMI.Net.Remoter;
using CoolRMI.Net.Streams;

namespace CoolRMI.Net
{
    internal class CoolRMIServe : CoolRMIRemoter
    {
        private readonly IConnection sock;

        public CoolRMIServe(CoolRMIServer server, IConnection sock,
            bool guaranteeOrdering) : base(server.Serializer, guaranteeOrdering)
        {
            this.sock = sock;
        }

        public void Connect()
        {
            base.Connect(sock);
        }
    }

    public class CoolRMIServer : IDisposable
    {
        public CoolRMIServiceRegistry ServiceRegistry { get; set; } =
            new CoolRMIServiceRegistry();
        public int TimeoutMillis { get; set; }
        public AbstractSerializer Serializer { get; set; }

        private readonly IConnectionServerFactory serverFactory;
        private readonly bool guaranteeOrdering;
        private IConnectionServer socket;
        private Thread thread;
        private bool exit;

        public CoolRMIServer(IConnectionServerFactory serverFactory,
            bool guaranteeOrdering)
        {
            this.serverFactory = serverFactory;
            this.guaranteeOrdering = guaranteeOrdering;
        }

        public CoolRMIServer(AbstractSerializer serializer, int port,
            bool guaranteeOrdering)
        {
            Serializer = serializer;
            serverFactory =
                new TcpServerFactory(new IPEndPoint(IPAddress.Any, port));
            this.guaranteeOrdering = guaranteeOrdering;
        }

        /// <summary>
        /// Start listening for clients
        /// </summary>
        public void Start()
        {
            if (thread != null)
                throw new InvalidOperationException("Thread already started");
            socket = serverFactory.BindServer();
            thread = new Thread(ThreadMethod) {Name = "CoolRMI server"};
            thread.Start();
        }

        public void Dispose()
        {
            exit = true;
            thread?.Join();
            socket?.Dispose();
        }

        private void ThreadMethod()
        {
            try
            {
                while (!exit)
                {
                    var sock = socket.Accept();
                    var serve =
                        new CoolRMIServe(this, sock, guaranteeOrdering)
                        {
                            TimeoutMillis = TimeoutMillis,
                            ServiceRegistry = ServiceRegistry
                        };
                    serve.Connect();
                }
            }
            finally
            {
                socket.Dispose();
            }
        }
    }
}
