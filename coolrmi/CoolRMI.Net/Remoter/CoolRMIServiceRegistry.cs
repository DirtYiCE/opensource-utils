using System;
using System.Collections.Concurrent;
using System.Collections.Generic;

namespace CoolRMI.Net.Remoter
{
    public class CoolRMIServiceRegistry
    {
        private readonly IDictionary<string, CoolRMIService>
            servicesReg = new ConcurrentDictionary<string, CoolRMIService>();

        private readonly IDictionary<Type, Type> proxyTypes =
            new ConcurrentDictionary<Type, Type>();

        public void AddService(CoolRMIService service)
        {
            servicesReg.Add(service.Name, service);
        }

        public CoolRMIService GetService(string name)
        {
            return servicesReg[name];
        }

        public void RemoveService(string name)
        {
            servicesReg.Remove(name);
        }

        public void AddProxyType(Type toBeProxied, Type proxyInterface)
        {
            proxyTypes.Add(toBeProxied, proxyInterface);
        }

        public Type GetProxyType(Type toBeProxied)
        {
            Type ret;
            return proxyTypes.TryGetValue(toBeProxied, out ret) ? ret : null;
        }
    }
}
