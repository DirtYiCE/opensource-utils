using System;
using System.IO;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMICreateProxy : AbstractCoolRMIMessage, ICustomPortableSerializable
    {
        public long ProxyId { get; }
        public Type Iface { get; }
        public override string Name => ToString();

        public CoolRMICreateProxy(long queryId, long proxyId,
            Type iface) : base(queryId)
        {
            ProxyId = proxyId;
            Iface = iface;
        }

        public override string ToString()
        {
            return "Create proxy: " + ProxyId + " " + Iface.AssemblyQualifiedName;
        }

        // ReSharper disable once UnusedMember.Local
        private CoolRMICreateProxy(PortableSerializer serializer,
            BinaryReader br)
        {
            ObjectSerializer.DefaultDeserialize(serializer, br, this,
                typeof(AbstractCoolRMIMessage));
            ProxyId = br.ReadInt64();
            Iface = serializer.ReadClassName(br);
        }

        void ICustomPortableSerializable.Serialize(
            PortableSerializer serializer, BinaryWriter bw)
        {
            ObjectSerializer.DefaultSerialize(serializer, bw, this,
                typeof(AbstractCoolRMIMessage));
            bw.Write(ProxyId);
            serializer.WriteClassName(bw, Iface);
        }
    }

    [Serializable]
    public class CoolRMICreateProxyReply : AbstractCoolRMIReply
    {
        public override string Name => ToString();

        public CoolRMICreateProxyReply(long queryId) : base(queryId) {}

        public override string ToString()
        {
            return "proxy created";
        }
    }
}
