using System;

namespace CoolRMI.Net.Streams
{
    public interface IConnectionClientFactory
    {
        IConnection Connect();
    }
}
