using System;

namespace CoolRMI.Net
{
    public class CoolRMIService : CoolRMIShareableObject
    {
        public string Name { get; }

        public CoolRMIService(string name, Type interface_, object service)
            : base(interface_, service)
        {
            Name = name;
        }
    }
}
