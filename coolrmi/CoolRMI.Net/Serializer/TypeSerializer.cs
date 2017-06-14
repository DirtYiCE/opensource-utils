using System;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    public enum TypeId
    {
        Null,
        Bool,
        Int8,
        Int16,
        Int32,
        Int64,
        Char,
        Float,
        Double,
        Enum,
        String,
        Object,
        Array,
        Exception,
        List,
    }

    public abstract class TypeSerializer
    {
        public TypeId Type { get; }
        public Type DotNetType { get; }

        protected TypeSerializer(TypeId type, Type dotNetType)
        {
            Type = type;
            DotNetType = dotNetType;
        }

        public virtual void WriteType(PortableSerializer serializer,
            BinaryWriter bw, Type typ)
        {
            bw.Write((byte) Type);
        }

        public virtual Type ReadType(PortableSerializer serializer,
            BinaryReader br)
        {
            return DotNetType;
        }

        public abstract void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ);

        public abstract object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ);

        public virtual bool CanSerializeIsSpecial => false;

        public virtual bool CanSerialize(PortableSerializer serializer, Type typ)
        {
            return typ == DotNetType;
        }

        public virtual bool IsPolymorphic => false;
    }
}
