using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text.RegularExpressions;

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
                       typeof(PortableSerializableAttribute)) != null ||
                   typeof(ICustomPortableSerializable).IsAssignableFrom(typ);
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


        private const BindingFlags GetBindingFlags =
            BindingFlags.DeclaredOnly | BindingFlags.Instance |
            BindingFlags.Public | BindingFlags.NonPublic;

        private static readonly ConcurrentDictionary<Type, Dictionary<string, FieldInfo>>
            TypeCache = new ConcurrentDictionary<Type, Dictionary<string, FieldInfo>>();

        private static readonly Regex BackingFieldPattern = new Regex(@"^<(.*)>k__BackingField$");

        private static Dictionary<string, FieldInfo> GetTypeCache(Type typ)
        {
            Dictionary<string, FieldInfo> ret;
            if (TypeCache.TryGetValue(typ, out ret)) return ret;

            ret = new Dictionary<string, FieldInfo>();
            var fields = typ.GetFields(GetBindingFlags);
            foreach (var f in fields)
            {
                if (f.IsNotSerialized) continue;
                var nameAttr =
                    f.GetCustomAttribute<PortableFieldNameAttribute>();

                string name;
                Match m;
                if (nameAttr != null) name = nameAttr.Name;
                else if ((m = BackingFieldPattern.Match(f.Name)) != Match.Empty)
                {
                    name = m.Groups[1].Value;
                    // get attribute of the property
                    nameAttr = typ.GetProperty(name, GetBindingFlags)
                        .GetCustomAttribute<PortableFieldNameAttribute>();
                    if (nameAttr != null) name = nameAttr.Name;
                }
                else name = f.Name;

                ret.Add(name, f);
            }

            // ignore if another thread already added it
            TypeCache.TryAdd(typ, ret);
            return ret;
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            var cust = o as ICustomPortableSerializable;
            if (cust != null) cust.Serialize(serializer, bw);
            else DefaultSerialize(serializer, bw, o, typ);
        }

        public static void DefaultSerialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            while (Attribute.GetCustomAttribute(typ,
                       typeof(PortableSerializableAttribute)) != null)
            {
                var cache = GetTypeCache(typ);
                foreach (var c in cache)
                {
                    bw.WritePString(c.Key);
                    serializer.Serialize(bw, c.Value.GetValue(o), c.Value.FieldType);
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

            var ctor = typ.GetConstructor(GetBindingFlags, null, EmptyTypes, null);
            ctor.Invoke(obj, EmptyObjects);

            return obj;
        }

        private static readonly Type[] DeserializerArgs = {
            typeof(PortableSerializer),
            typeof(BinaryReader)
        };
        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            if (typeof(ICustomPortableSerializable).IsAssignableFrom(typ))
            {
                var ctor = typ.GetConstructor(GetBindingFlags, null,
                    DeserializerArgs, null);
                if (ctor != null)
                    return ctor.Invoke(new object[] {serializer, br});
                var stat = typ.GetMethod("Deserialize",
                    BindingFlags.Static | BindingFlags.Public |
                    BindingFlags.NonPublic, null, DeserializerArgs, null);
                if (stat != null)
                    return stat.Invoke(null, new object[] {serializer, br});
                throw new Exception("Invalid ICustomPortableSerializable");
            }
            else
            {
                var instance = CreateObject(typ);
                DefaultDeserialize(serializer, br, instance, typ);
                return instance;
            }
        }

        public static void DefaultDeserialize(PortableSerializer serializer,
            BinaryReader br, object o, Type typ)
        {
            string fieldName;
            var cache = GetTypeCache(typ);
            while ((fieldName = br.ReadPString()) != null)
            {
                FieldInfo field;
                while (!cache.TryGetValue(fieldName, out field))
                {
                    typ = typ.BaseType;
                    cache = GetTypeCache(typ);
                }

                field.SetValue(o, serializer.Deserialize(br, field.FieldType));
            }
        }
    }
}
