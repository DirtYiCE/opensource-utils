using System;

namespace CoolRMI.Net.Example.Client
{
    public class Client
    {
        public static void Main(string[] args)
        {
            var serializer = Utils.GetSerializer(args);
            var c = new CoolRMIClient(serializer, "localhost", 9000, true);
            c.ServiceRegistry.AddProxyType(typeof(CallbackImpl), typeof(ICallback));
            var s = c.GetService<IService>("ExampleServiceV0.0.1");
            Console.WriteLine(s.echo("foo", 3));

            try
            {
                s.exceptionExample();
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }

            Console.WriteLine(DateTime.Now + " cb calling");
            s.initTimer(new CallbackImpl(c, s), 5000);
        }
    }

    public class CallbackImpl : ICallback
    {
        private readonly CoolRMIClient c;
        private readonly IService service;

        public CallbackImpl(CoolRMIClient c, IService service)
        {
            this.c = c;
            this.service = service;
        }

        public void callback(string s)
        {
            Console.WriteLine(DateTime.Now + " cb returned: " + s);
            ((ICoolRMIProxy) service).Dispose();
            c.Dispose();
        }
    }
}
