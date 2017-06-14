using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;

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
            return serializer.ReadClassName(br);
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            var exc = (Exception) o;
            bw.WritePString(exc.Message);

            var trace = new StackTrace(exc, true);
            bw.Write(trace.FrameCount);
            for (int i = 0; i < trace.FrameCount; ++i)
            {
                var item = trace.GetFrame(i);
                var meth = item.GetMethod();
                if (meth == null)
                {
                    bw.WritePString(null);
                    bw.WritePString(null);
                }
                else
                {
                    bw.WritePString(serializer.GetPortableClassName(meth.DeclaringType));
                    bw.WritePString(meth.Name);
                }
                bw.WritePString(item.GetFileName());
                bw.Write(item.GetFileLineNumber());
            }

            serializer.Serialize(bw, exc.InnerException, typeof(Exception));
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var message = br.ReadPString();
            var len = br.ReadInt32();

            // todo...
            var sb = new StringBuilder();
            for (int i = 0; i < len; ++i)
            {
                sb.AppendFormat("  at {0}.{1} in {2}:{3}\n", br.ReadPString(),
                    br.ReadPString(), br.ReadPString() ?? "<filename unknown>",
                    br.ReadInt32());
            }

            var cause = serializer.Deserialize(br, typeof(Exception));

            var inst = Activator.CreateInstance(typ, message, cause);
            typ.GetField("_remoteStackTraceString",
                    BindingFlags.Instance | BindingFlags.NonPublic)
                .SetValue(inst, sb.ToString());
            return inst;
        }
    }
}
