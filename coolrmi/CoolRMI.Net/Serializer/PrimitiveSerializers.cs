using System;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    public class NullSerializer : TypeSerializer
    {
        public NullSerializer() : base(TypeId.Null, null) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ) {}

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return null;
        }
    }

    public class BoolSerializer : TypeSerializer
    {
        public BoolSerializer() : base(TypeId.Bool, typeof(bool)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((bool) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadBoolean();
        }
    }

    public class ByteSerializer : TypeSerializer
    {
        public ByteSerializer() : base(TypeId.Int8, typeof(sbyte)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((sbyte) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadSByte();
        }
    }

    public class ShortSerializer : TypeSerializer
    {
        public ShortSerializer() : base(TypeId.Int16, typeof(short)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((short) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadInt16();
        }
    }

    public class IntSerializer : TypeSerializer
    {
        public IntSerializer() : base(TypeId.Int32, typeof(int)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((int) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadInt32();
        }
    }

    public class LongSerializer : TypeSerializer
    {
        public LongSerializer() : base(TypeId.Int64, typeof(long)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((long) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadInt64();
        }
    }

    public class CharSerializer : TypeSerializer
    {
        public CharSerializer() : base(TypeId.Char, typeof(char)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((char) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadChar();
        }
    }

    public class FloatSerializer : TypeSerializer
    {
        public FloatSerializer() : base(TypeId.Float, typeof(float)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((float) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadSingle();
        }
    }

    public class DoubleSerializer : TypeSerializer
    {
        public DoubleSerializer() : base(TypeId.Double, typeof(double)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.Write((double) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadDouble();
        }
    }

    public class StringSerializer : TypeSerializer
    {
        public StringSerializer() : base(TypeId.String, typeof(string)) {}

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            bw.WritePString((string) o);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            return br.ReadPString();
        }
    }
}
