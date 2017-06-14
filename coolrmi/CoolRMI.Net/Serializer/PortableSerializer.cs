using System;
using System.Collections.Generic;
using System.IO;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Serializer
{
    public class PortableSerializer : AbstractSerializer
    {
        private static readonly TypeSerializer[] Serializers = {
            /*Null*/  new NullSerializer(),
            /*Bool*/  new BoolSerializer(),
            /*Int8*/  new ByteSerializer(),
            /*Int16*/ new ShortSerializer(),
            /*Int32*/ new IntSerializer(),
            /*Int64*/ new LongSerializer(),
            /*Char*/  new CharSerializer(),
            /*Float*/ new FloatSerializer(),
            /*Double*/new DoubleSerializer(),
            /*Enum*/  new EnumSerializer(),
            /*String*/new StringSerializer(),
            /*Object*/new ObjectSerializer(),
            /*Array*/ new ArraySerializer(),
            /*Exception*/ new ExceptionSerializer(),
            /*List*/  new ListSerializer(),
        };

        private static readonly Dictionary<Type, TypeSerializer> ClassMap =
            new Dictionary<Type, TypeSerializer>();
        private static readonly List<TypeSerializer> SpecialSerializers =
            new List<TypeSerializer>();

        private readonly Dictionary<string, string> dotNetToPortableNameMap =
            new Dictionary<string, string>();
        private readonly Dictionary<string, string> portableToDotNetNameMap =
            new Dictionary<string, string>();

        #region Replacer

        private readonly Dictionary<Type, CoolRMIReplaceEntry> replaceTypes =
            new Dictionary<Type, CoolRMIReplaceEntry>();

        public void AddReplaceType(CoolRMIReplaceEntry entry)
        {
            replaceTypes.Add(entry.TypeToReplace, entry);
        }

        public object ReplaceObject(object obj)
        {
            if (obj == null) return null;

            var replacer = GetReplacer(obj.GetType());
            return replacer == null ? obj : replacer.DoReplace(obj);
        }

        public CoolRMIReplaceEntry GetReplacer(Type type)
        {
            CoolRMIReplaceEntry found;
            if (replaceTypes.TryGetValue(type, out found)) return found;

            foreach (var e in replaceTypes.Values)
            {
                if (e != null && e.TypeToReplace.IsAssignableFrom(type))
                {
                    found = e;
                    break;
                }
            }

            replaceTypes.Add(type, found);
            return found;
        }

        #endregion

        public void AddMapping(string dotNet, string portable)
        {
            dotNetToPortableNameMap.Add(dotNet, portable);
            portableToDotNetNameMap.Add(portable, dotNet);
        }

        static PortableSerializer()
        {
            for (int i = 0; i < Serializers.Length; ++i)
            {
                var s = Serializers[i];
                if (s == null) continue;

                if ((int) s.Type != i)
                    throw new InvalidProgramException("Invalid serializers");

                if (s.DotNetType != null) ClassMap.Add(s.DotNetType, s);
                if (s.CanSerializeIsSpecial) SpecialSerializers.Add(s);
            }
        }

        public override byte[] Serialize(object o)
        {
            var memstream = new MemoryStream();
            var bw = new BinaryWriter(memstream);
            Serialize(bw, o, null);
            bw.Flush();
            return memstream.GetBuffer();
        }

        internal TypeSerializer GetSerializer(Type typ)
        {
            if (GetReplacer(typ) != null) typ = typeof(IReplaceSerializable);

            TypeSerializer s;
            if (ClassMap.TryGetValue(typ, out s)) return s;

            foreach (var sc in SpecialSerializers)
                if (sc.CanSerialize(this, typ))
                    return sc;
            throw new Exception("Unserializable class " + typ);
        }

        internal TypeSerializer GetSerializer(int idx)
        {
            return Serializers[idx];
        }

        internal void Serialize(BinaryWriter bw, object o, Type typ)
        {
            TypeSerializer s;
            o = ReplaceObject(o);

            bool full = typ == null;
            if (typ == null && o == null)
            {
                s = Serializers[(int) TypeId.Null];
            }
            else
            {
                if (typ == null) typ = o.GetType();
                s = GetSerializer(typ);
                if (s.IsPolymorphic)
                {
                    full = true;
                    if (o != null)
                    {
                        typ = o.GetType();
                        s = GetSerializer(typ);
                    }
                    else
                    {
                        s = Serializers[(int) TypeId.Null];
                    }
                }
            }

            if (full) s.WriteType(this, bw, typ);
            s.Serialize(this, bw, o, typ);
        }

        public override object Deserialize(byte[] data)
        {
            var memstream = new MemoryStream(data);
            return Deserialize(new BinaryReader(memstream), null);
        }

        internal object Deserialize(BinaryReader br, Type typ)
        {
            Type desType;
            TypeSerializer ser;

            if (typ != null)
            {
                ser = GetSerializer(typ);
                if (ser.IsPolymorphic)
                {
                    ser = GetSerializer(br.ReadByte());
                    desType = ser.ReadType(this, br);
                }
                else
                {
                    desType = typ;
                }
            }
            else
            {
                ser = GetSerializer(br.ReadByte());
                desType = ser.ReadType(this, br);
            }

            var ret = ser.Deserialize(this, br, desType);
            var serializable = ret as IReplaceSerializable;
            return serializable != null ? serializable.ReadResolve() : ret;
        }

        internal string GetPortableClassName(Type typ)
        {
            if (typ.IsGenericType) typ = typ.GetGenericTypeDefinition();
            return GetPortableClassName(typ.AssemblyQualifiedName);
        }

        internal string GetPortableClassName(string name)
        {
            string name2;
            return dotNetToPortableNameMap.TryGetValue(name, out name2)
                ? name2 : name;
        }

        internal void WriteClassName(BinaryWriter bw, Type typ)
        {
            bw.WritePString(GetPortableClassName(typ));

            foreach (var t in typ.GenericTypeArguments)
                WriteClassName(bw, t);
        }

        internal string GetDotNetClassName(string name)
        {
            string name2;
            return portableToDotNetNameMap.TryGetValue(name, out name2)
                ? name2 : name;
        }

        internal Type ReadClassName(BinaryReader rd)
        {
            var typ = Type.GetType(GetDotNetClassName(rd.ReadPString()));
            if (typ.IsGenericType)
            {
                var args = typ.GetGenericArguments();
                for (int i = 0; i < args.Length; ++i)
                    args[i] = ReadClassName(rd);
                typ = typ.MakeGenericType(args);
            }
            return typ;
        }
    }
}
