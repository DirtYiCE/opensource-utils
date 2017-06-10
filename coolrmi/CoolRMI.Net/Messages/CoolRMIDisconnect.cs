using System;
using System.Threading.Tasks;

namespace CoolRMI.Net.Messages
{
    [Serializable]
    public class CoolRMIDisconnect : AbstractCoolRMIMessage
    {
        public override string Name => "Disconnect";

        [NonSerialized]
        private readonly TaskCompletionSource<object> src =
            new TaskCompletionSource<object>();


        public void WaitSent(int timeoutMillis)
        {
            src.Task.Wait(timeoutMillis);
        }

        public override void Sent()
        {
            src.SetResult(null);
        }
    }
}
