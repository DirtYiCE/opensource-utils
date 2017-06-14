using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;

namespace CoolRMI.Net.Serializer
{
    public class ListSerializer : TypeSerializer
    {
         public ListSerializer() : base(TypeId.List, null) {}

        public override bool CanSerializeIsSpecial => true;
        public override bool CanSerialize(PortableSerializer serializer, Type typ)
        {
            return typeof(IList<>).IsAssignableFrom(typ);
        }

        private static Type GetElementType(Type typ)
        {
            return typ.GetInterface("System.Collections.Generic.IList`1")
                .GetGenericArguments()[0];
        }

        public override void WriteType(PortableSerializer serializer,
            BinaryWriter bw, Type typ)
        {
            base.WriteType(serializer, bw, typ);
            serializer.WriteClassName(bw, GetElementType(typ));
        }

        public override Type ReadType(PortableSerializer serializer, BinaryReader br)
        {
            var items = serializer.ReadClassName(br);
            return typeof(List<>).MakeGenericType(items);
        }

        public override void Serialize(PortableSerializer serializer,
            BinaryWriter bw, object o, Type typ)
        {
            if (o == null)
            {
                bw.Write(-1);
                return;
            }

            // todo: IList<> csak IEnumerable-t implemental, nem IList-et...
            var lst = (IList) o;
            bw.Write(lst.Count);

            var elemType = GetElementType(typ);
            foreach (var el in lst)
                serializer.Serialize(bw, el, elemType);
        }

        public override object Deserialize(PortableSerializer serializer,
            BinaryReader br, Type typ)
        {
            var len = br.ReadInt32();
            if (len == -1) return null;

            var inst = (IList) Activator.CreateInstance(typ, len);
            var elemType = GetElementType(typ);

            for (int i = 0; i < len; ++i)
                inst.Add(serializer.Deserialize(br, elemType));

            return inst;
        }
    }
}
