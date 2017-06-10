using System;

namespace CoolRMI.Net.Streams
{
    public interface IConnectionServer : IDisposable
    {
        IConnection Accept();
    }

    public interface IConnectionServerFactory
    {
        IConnectionServer BindServer();
    }
}
