using CoolRMI.Net.Remoter;

namespace CoolRMI.Net
{
    public interface ICoolRMIServerSideProxy
    {
        CoolRMIServerSideObject ServerSideProxyObject { get; }
    }
}
