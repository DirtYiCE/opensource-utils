using System.IO;
using System.Net;
using System.Net.Sockets;

namespace CoolRMI.Net.Streams
{
    public class TcpConnection : IConnection
    {
        private readonly TcpClient client;

        public TcpConnection(TcpClient client)
        {
            this.client = client;
        }

        public void Dispose()
        {
            client.Close();
        }

        public Stream GetStream()
        {
            return client.GetStream();
        }
    }

    public class TcpServer : IConnectionServer
    {
        private readonly TcpListener listener;

        public TcpServer(TcpListener listener)
        {
            this.listener = listener;
        }

        public void Dispose()
        {
            listener.Stop();
        }

        public IConnection Accept()
        {
            return new TcpConnection(listener.AcceptTcpClient());
        }
    }

    public class TcpServerFactory : IConnectionServerFactory
    {
        private readonly IPEndPoint endPoint;

        public TcpServerFactory(IPEndPoint endPoint)
        {
            this.endPoint = endPoint;
        }

        public IConnectionServer BindServer()
        {
            var listener = new TcpListener(endPoint);
            listener.Start();
            return new TcpServer(listener);
        }
    }

    public class TcpConnectionFactory : IConnectionClientFactory
    {
        private readonly string hostname;
        private readonly int port;

        public TcpConnectionFactory(string hostname, int port)
        {
            this.hostname = hostname;
            this.port = port;
        }

        public IConnection Connect()
        {
            return new TcpConnection(new TcpClient(hostname, port));
        }
    }
}
