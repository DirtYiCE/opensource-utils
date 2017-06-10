using System;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net
{
    public interface ICoolRMIProxy : IDisposable
    {
        bool IsProxyDisposed { get; }
        CoolRMIRemoter ProxyHome { get; }
        CoolRMIProxy ProxyObject { get; }
    }
}
