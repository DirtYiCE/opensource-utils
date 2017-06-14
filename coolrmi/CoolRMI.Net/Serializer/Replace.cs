using System;

namespace CoolRMI.Net.Serializer
{
    public abstract class CoolRMIReplaceEntry
    {
        public Type TypeToReplace { get; }

        protected CoolRMIReplaceEntry(Type typeToReplace)
        {
            TypeToReplace = typeToReplace;
        }

        public abstract IReplaceSerializable DoReplace(object o);
    }

    public interface IReplaceSerializable
    {
        object ReadResolve();
    }
}
