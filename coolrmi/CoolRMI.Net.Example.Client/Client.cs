using System;
using CoolRMI.Net.Remoter;

namespace CoolRMI.Net.Example.Client
{
    public class Client
    {
        public static void Main(string[] args)
        {
            var c = new CoolRMIClient(new DotNetSerializer(), "localhost", 5656,
                true);
            c.ServiceRegistry.AddProxyType(typeof(CallbackImpl), typeof(ICallback));
            var s = c.GetService<IService>("TestService");
            Console.WriteLine(s.Echo("foo", 3));

            try
            {
                s.ThrowException();
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }

            Console.WriteLine(DateTime.Now + " cb calling");
            s.InitTimer(new CallbackImpl(c, s), 5000);
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

        public void Callback(string s)
        {
            Console.WriteLine(DateTime.Now + " cb returned: " + s);
            ((ICoolRMIProxy) service).Dispose();
            c.Dispose();
        }
    }
}
