using System;

namespace CoolRMI.Net
{
    public class CoolRMIShareableObject
    {
        public Type Interface { get; }
        public object Service { get; }

        public CoolRMIShareableObject(Type interface_, object service)
        {
            Interface = interface_;
            Service = service;
        }
    }
}
