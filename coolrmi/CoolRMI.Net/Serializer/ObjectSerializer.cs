using System;
using System.IO;
using System.Reflection;
using System.Runtime.Serialization;

namespace CoolRMI.Net.Serializer
{
    public class ObjectSerializer : TypeSerializer
    {
        public ObjectSerializer() : base(TypeId.Object, typeof(object)) {}

        public override bool CanSerializeIsSpecial => true;
        public override bool CanSerialize(PortableSerializer serializer, Type typ)
        {
            return typ == typeof(object) ||
                   Attribute.GetCustomAttribute(typ,
                       typeof(PortableSerializableAttribute)) != null;
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


        private const BindingFlags getBindingFlags =
            BindingFlags.DeclaredOnly | BindingFlags.Instance |
            BindingFlags.Public | BindingFlags.NonPublic;

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            while (Attribute.GetCustomAttribute(typ,
                       typeof(PortableSerializableAttribute)) != null)
            {
                var fields = typ.GetFields(getBindingFlags);
                foreach (var f in fields)
                {
                    if (f.IsNotSerialized) continue;
                    bw.WritePString(f.Name);
                    serializer.Serialize(bw, f.GetValue(o), f.FieldType);
                }

                typ = typ.BaseType;
            }

            bw.WritePString(null);
        }

        private static readonly Type[] EmptyTypes = new Type[0];
        private static readonly object[] EmptyObjects = new object[0];
        private static object CreateObject(Type typ)
        {
            var obj = FormatterServices.GetUninitializedObject(typ);

            while (Attribute.GetCustomAttribute(typ,
                       typeof(PortableSerializableAttribute)) != null)
            {
                typ = typ.BaseType;
            }

            var ctor = typ.GetConstructor(getBindingFlags, null, EmptyTypes, null);
            ctor.Invoke(obj, EmptyObjects);

            return obj;
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var instance = CreateObject(typ);

            string fieldName;
            while ((fieldName = br.ReadPString()) != null)
            {
                var field = typ.GetField(fieldName, getBindingFlags);
                while (field == null)
                {
                    typ = typ.BaseType;
                    field = typ.GetField(fieldName, getBindingFlags);
                }

                field.SetValue(instance, serializer.Deserialize(br, field.FieldType));
            }

            return instance;
        }
    }
}
