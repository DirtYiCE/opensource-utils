using System;

namespace CoolRMI.Net.Remoter
{
    public class CoolRMIServerSideObject : IDisposable
    {
        public long ProxyId { get; }
        public Type Iface { get; }
        public object Service { get; }
        public bool IsDisposed { get; private set; }
        public event Action<object> Disposed;

        public CoolRMIServerSideObject(long id, Type iface, object service)
        {
            ProxyId = id;
            Iface = iface;
            Service = service;
        }

        public void Dispose()
        {
            IsDisposed = true;
            Disposed?.Invoke(this);
        }
    }
}
