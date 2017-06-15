using System;
using CoolRMI.Net.Remoter;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Example
{
    public static class Utils
    {
        public static AbstractSerializer GetSerializer(string[] args)
        {
            var serializer = "portable";
            if (args.Length >= 1) serializer = args[0];

            switch (serializer)
            {
                case "portable":
                    var ser = new PortableSerializer();
                    ser.AddNamespaceMapping("CoolRMI.Net.Example",
                        typeof(ICallback).Assembly.GetName(),
                        "coolrmi.example");
                    return ser;
                case "dotnet":
                    return new DotNetSerializer();
                default:
                    throw new Exception("Unknown serializer " + serializer);
            }
        }
    }
}
