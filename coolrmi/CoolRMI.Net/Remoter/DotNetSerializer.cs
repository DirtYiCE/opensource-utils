using System.IO;
using System.Runtime.Serialization.Formatters.Binary;

namespace CoolRMI.Net.Remoter
{
    public class DotNetSerializer : AbstractSerializer
    {
        public override byte[] Serialize(object o)
        {
            var formatter = new BinaryFormatter();
            using (var stream = new MemoryStream())
            {
                formatter.Serialize(stream, o);
                return stream.GetBuffer();
            }
        }

        public override object Deserialize(byte[] data)
        {
            var formatter = new BinaryFormatter();
            using (var stream = new MemoryStream(data))
            {
                return formatter.Deserialize(stream);
            }
        }
    }
}
