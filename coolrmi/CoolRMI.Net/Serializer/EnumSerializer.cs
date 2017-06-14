using System;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    public class EnumSerializer : TypeSerializer
    {
        public EnumSerializer() : base(TypeId.Enum, null) {}

        public override bool CanSerializeIsSpecial => true;

        public override bool CanSerialize(PortableSerializer serializer,
            Type typ)
        {
            return typ.IsEnum;
        }

        public override void WriteType(PortableSerializer serializer,
            BinaryWriter bw, Type typ)
        {
            base.WriteType(serializer, bw, typ);
            serializer.WriteClassName(bw, typ);
        }

        public override Type ReadType(PortableSerializer serializer,
            BinaryReader br)
        {
            return serializer.ReadClassName(br);
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.WritePString(Enum.GetName(typ, o));
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var names = typ.GetEnumNames();
            var vals = typ.GetEnumValues();
            var i = Array.IndexOf(names, br.ReadPString());
            return vals.GetValue(i);
        }
    }
}
