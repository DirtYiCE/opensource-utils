using System;

namespace CoolRMI.Net.Serializer
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Struct,
        Inherited = true, AllowMultiple = false)]
    public class PortableSerializableAttribute : Attribute
    {

    }
}
