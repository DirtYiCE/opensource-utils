using System;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Remoting.Proxies;

namespace CoolRMI.Net.Remoter
{
    public class CoolRMIServerSideProxy : RealProxy, IRemotingTypeInfo
    {
        private readonly CoolRMIServerSideObject obj;

        public ICoolRMIServerSideProxy ProxyObject =>
            (ICoolRMIServerSideProxy) GetTransparentProxy();

        public CoolRMIServerSideProxy(CoolRMIRemoter home,
            CoolRMIServerSideObject obj) : base(obj.Iface)
        {
            this.obj = obj;
        }

        public override IMessage Invoke(IMessage msg)
        {
            var imsg = (IMethodCallMessage) msg;
            if (imsg.MethodName == "get_ServerSideProxyObject")
                return new ReturnMessage(obj, null, 0, imsg.LogicalCallContext, imsg);

            return new ReturnMessage(
                new ArgumentException("invalid CoolRMIServerSideProxy usage"),
                imsg);
        }

        // IRemotingTypeInfo
        public bool CanCastTo(Type fromType, object o)
        {
            return fromType == typeof(ICoolRMIServerSideProxy) ||
                   fromType == GetProxiedType();
        }
        public string TypeName { get; set; }
    }
}
