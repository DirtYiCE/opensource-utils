using System.IO;
using System.Text;

namespace CoolRMI.Net.Serializer
{
    public static class Utils
    {
        public static void WritePString(this BinaryWriter bw, string s)
        {
            if (s == null)
            {
                bw.Write(-1);
                return;
            }

            var bytes = Encoding.UTF8.GetBytes(s);
            bw.Write(bytes.Length);
            bw.Write(bytes);
        }

        public static string ReadPString(this BinaryReader br)
        {
            var len = br.ReadInt32();
            if (len == -1) return null;

            var bytes = br.ReadBytes(len);
            return Encoding.UTF8.GetString(bytes);
        }
    }
}
