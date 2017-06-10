using CoolRMI.Net.Messages;

namespace CoolRMI.Net.Multiplexer
{
    public struct SocketMultiplexerSource
    {
        public byte[] ToSend { get; }
        private readonly AbstractCoolRMIMessage message;

        public SocketMultiplexerSource(byte[] toSend,
            AbstractCoolRMIMessage message)
        {
            ToSend = toSend;
            this.message = message;
        }

        public override string ToString()
        {
            return "Socket multiplexer source: " + message.Name;
        }

        /// <summary>
        /// Callback when the last piece of this message has been sent through the (TCP) channel.
        /// </summary>
        public void Sent() {
            message.Sent();
        }
    }
}
