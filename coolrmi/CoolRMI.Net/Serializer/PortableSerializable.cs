using System;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Struct,
        Inherited = true, AllowMultiple = false)]
    public class PortableSerializableAttribute : Attribute
    {
    }

    [AttributeUsage(AttributeTargets.Field | AttributeTargets.Property)]
    public class PortableFieldNameAttribute : Attribute
    {
        public string Name { get; }

        public PortableFieldNameAttribute(string name)
        {
            Name = name;
        }
    }

    public interface ICustomPortableSerializable
    {
        // ctor: PortableSerializer, BinaryReader
        // or: public static Deserialize(PortableSerializer, BinaryReader)

        void Serialize(PortableSerializer serializer, BinaryWriter bw);
    }
}
