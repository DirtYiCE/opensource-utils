using System.Threading;
using CoolRMI.Net.Messages;
using CoolRMI.Net.Remoter;
using CoolRMI.Net.Streams;

namespace CoolRMI.Net
{
    public class CoolRMIClient : CoolRMIRemoter
    {
        private readonly IConnectionClientFactory connectionFactory;

        public CoolRMIClient(AbstractSerializer serializer, string hostname,
            int port, bool guaranteeOrdering) : base(serializer,
            guaranteeOrdering)
        {
            connectionFactory = new TcpConnectionFactory(hostname, port);
            Connect();
        }

        public CoolRMIClient(AbstractSerializer serializer,
            IConnectionClientFactory connectionFactory, bool guaranteeOrdering)
            : base(serializer, guaranteeOrdering)
        {
            this.connectionFactory = connectionFactory;
            Connect();
        }

        private void Connect()
        {
            base.Connect(connectionFactory.Connect());
        }

        // ReSharper disable once RedundantDefaultMemberInitializer
        private int disconnectSent = 0;
        public override void Dispose()
        {
            var alreadySent = Interlocked.Exchange(ref disconnectSent, 1);
            if (alreadySent != 0) return;

            var disconnect = new CoolRMIDisconnect();
            Send(disconnect);
            disconnect.WaitSent(TimeoutMillis);
            base.Dispose();
        }
    }
}
