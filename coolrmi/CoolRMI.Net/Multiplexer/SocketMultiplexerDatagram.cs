using System;
using System.IO;

namespace CoolRMI.Net.Multiplexer
{
    public class SocketMultiplexerDatagram
    {
        public byte[] Content { get; set; }

        public SocketMultiplexerDatagram(byte[] content)
        {
            Content = content;
        }

        public override string ToString()
        {
            return "datagram: " + Content.Length;
        }

        public static SocketMultiplexerDatagram ReadFromStream(Stream stream)
        {
            var buf = new byte[4];
            ReadAll(stream, buf);
            if (!BitConverter.IsLittleEndian) Array.Reverse(buf);
            var len = BitConverter.ToInt32(buf, 0);

            buf = new byte[len];
            ReadAll(stream, buf);
            return new SocketMultiplexerDatagram(buf);
        }

        public void WriteToStream(Stream stream)
        {
            var buf = BitConverter.GetBytes(Content.Length);
            if (!BitConverter.IsLittleEndian) Array.Reverse(buf);
            stream.Write(buf, 0, 4);
            stream.Write(Content, 0, Content.Length);
        }

        private static void ReadAll(Stream stream, byte[] buf)
        {
            var at = 0;
            while (at < buf.Length)
            {
                var n = stream.Read(buf, at, buf.Length - at);
                if (n == 0)
                {
                    throw new EndOfStreamException();
                }
                at += n;
            }
        }
    }
}
