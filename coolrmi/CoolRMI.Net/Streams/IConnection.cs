using System;
using System.IO;

namespace CoolRMI.Net.Streams
{
    public interface IConnection : IDisposable
    {
        Stream GetStream();
    }
}
