using System;
using System.Collections.Concurrent;
using System.IO;
using System.Threading;
using CoolRMI.Net.Messages;

namespace CoolRMI.Net.Multiplexer
{
    public interface ISocketMultiplexerListener
    {
        void MessageReceived(byte[] bs);
        void PipeBroken(Exception e);
    }

    public class SocketMultiplexer
    {
        private readonly ISocketMultiplexerListener listener;
        private readonly Stream stream;
        private bool exit;

        private readonly BlockingCollection<SocketMultiplexerSource>
            messagesToSend = new BlockingCollection<SocketMultiplexerSource>();

        public SocketMultiplexer(Stream stream, ISocketMultiplexerListener listener)
        {
            this.listener = listener;
            this.stream = stream;
        }

        public void Start()
        {
            new Thread(ReadThread) {Name = "CoolRMI read thread"}.Start();
            new Thread(WriteThread) {Name = "CoolRMI write thread"}.Start();
        }

        private void ReadThread()
        {
            try
            {
                while (!exit)
                {
                    var datagram =
                        SocketMultiplexerDatagram.ReadFromStream(stream);
                    listener.MessageReceived(datagram.Content);
                }
            }
            catch (Exception e)
            {
                listener.PipeBroken(e);
            }
        }

        private void WriteThread()
        {
            SocketMultiplexerSource source;
            while (messagesToSend.TryTake(out source, -1))
            {
                var datagram = new SocketMultiplexerDatagram(source.ToSend);
                try
                {
                    datagram.WriteToStream(stream);
                    stream.Flush();
                }
                catch (IOException e)
                {
                    listener.PipeBroken(e);
                }
                source.Sent();
            }
        }

        public void AddMessageToSend(byte[] content,
            AbstractCoolRMIMessage message)
        {
            messagesToSend.Add(new SocketMultiplexerSource(content, message));
        }

        public void Stop()
        {
            exit = true;
            messagesToSend.CompleteAdding();

            SocketMultiplexerSource source;
            while (messagesToSend.TryTake(out source))
            {
                source.Sent();
            }
        }
    }
}
