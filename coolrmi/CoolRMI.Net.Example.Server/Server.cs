using System;
using System.Threading.Tasks;

namespace CoolRMI.Net.Example.Server
{
    public class Server
    {
        public static void Main(string[] args)
        {
            var serializer = Utils.GetSerializer(args);
            var s = new CoolRMIServer(serializer, 9000, true);
            s.ServiceRegistry.AddService(new CoolRMIService(
                "ExampleServiceV0.0.1", typeof(IService), new Service()));
            s.Start();
        }
    }

    public class Service : IService
    {
        public string echo(string s, int x)
        {
            return s + " " + x;
        }

        public void exceptionExample()
        {
            throw new Exception("Test exception");
        }

        public void initTimer(ICallback cb, long timeoutMillis)
        {
            Task.Delay((int) timeoutMillis)
                .ContinueWith(t =>
                {
                    try
                    {
                        cb.callback("Server time now: " + DateTime.Now);
                        (cb as ICoolRMIProxy)?.Dispose();
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e);
                    }
                });
        }
    }
}
