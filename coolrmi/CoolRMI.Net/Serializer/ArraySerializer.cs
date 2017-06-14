using System;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    public class ArraySerializer : TypeSerializer
    {
        public ArraySerializer() : base(TypeId.Array, null) {}

        public override bool CanSerializeIsSpecial => true;
        public override bool CanSerialize(PortableSerializer serializer, Type typ)
        {
            return typ.IsArray;
        }

        public override void WriteType(PortableSerializer serializer,
            BinaryWriter bw, Type typ)
        {
            base.WriteType(serializer, bw, typ);
            var elems = typ.GetElementType();
            var elemSer = serializer.GetSerializer(elems);

            elemSer.WriteType(serializer, bw, elems);
        }

        public override Type ReadType(PortableSerializer serializer,
            BinaryReader br)
        {
            var ser = serializer.GetSerializer(br.Read());
            var elemTyp = ser.ReadType(serializer, br);
            return elemTyp.MakeArrayType();
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            if (o == null)
            {
                bw.Write(-1);
                return;
            }

            var ary = (Array) o;
            bw.Write(ary.Length);

            var serTyp = o.GetType().GetElementType();
            foreach (var x in ary)
            {
                serializer.Serialize(bw, x, serTyp);
            }
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var len = br.ReadInt32();
            if (len == -1) return null;

            var elemTyp = typ.GetElementType();
            var ret = Array.CreateInstance(elemTyp, len);
            for (int i = 0; i < len; ++i)
                ret.SetValue(serializer.Deserialize(br, elemTyp), i);

            return ret;
        }
    }
}
