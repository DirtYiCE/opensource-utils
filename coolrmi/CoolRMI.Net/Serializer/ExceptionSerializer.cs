using System;
using System.IO;
using System.Reflection;

namespace CoolRMI.Net.Serializer
{
    public class ExceptionSerializer : TypeSerializer
    {
        public ExceptionSerializer() : base(TypeId.Exception, typeof(Exception))
        {}

        public override bool CanSerializeIsSpecial => true;
        public override bool CanSerialize(PortableSerializer serializer, Type typ)
        {
            return typeof(Exception).IsAssignableFrom(typ);
        }

        public override bool IsPolymorphic => true;

        public override void WriteType(PortableSerializer serializer,
            BinaryWriter bw, Type typ)
        {
            base.WriteType(serializer, bw, typ);
            serializer.WriteClassName(bw, typ);
        }

        public override Type ReadType(PortableSerializer serializer, BinaryReader br)
        {
            return serializer.ReadClassName(br) ?? typeof(Exception);
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            var exc = (Exception) o;
            bw.WritePString(exc.Message);
            bw.WritePString(exc.StackTrace);
            serializer.Serialize(bw, exc.InnerException, typeof(Exception));
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var message = br.ReadPString();
            var trace = br.ReadPString();
            var cause = serializer.Deserialize(br, typeof(Exception));

            var inst = Activator.CreateInstance(typ, message, cause);
            typeof(Exception).GetField("_remoteStackTraceString",
                    BindingFlags.Instance | BindingFlags.NonPublic)
                .SetValue(inst, trace);
            return inst;
        }
    }
}
