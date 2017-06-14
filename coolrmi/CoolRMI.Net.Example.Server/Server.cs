using System;
using System.Threading.Tasks;
using CoolRMI.Net.Serializer;

namespace CoolRMI.Net.Example.Server
{
    public class Server
    {
        public static void Main(string[] args)
        {
            var s = new CoolRMIServer(new PortableSerializer(), 5656, true);
            s.ServiceRegistry.AddService(new CoolRMIService("TestService",
                typeof(IService), new Service()));
            s.Start();
        }
    }

    public class Service : IService
    {
        public string Echo(string s, int x)
        {
            return s + " " + x;
        }

        public void ThrowException()
        {
            throw new Exception("Test exception");
        }

        public void InitTimer(ICallback cb, int timeoutMillis)
        {
            Task.Delay(timeoutMillis)
                .ContinueWith(t =>
                {
                    cb.Callback("Server time now: "+DateTime.Now);
                    (cb as ICoolRMIProxy)?.Dispose();
                });
        }
    }
}
