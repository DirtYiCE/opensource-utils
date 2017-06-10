namespace CoolRMI.Net.Remoter
{
    public abstract class AbstractSerializer
    {
        public CoolRMIServiceRegistry ServiceRegistry { get; set; }

        public abstract byte[] Serialize(object o);
        public abstract object Deserialize(byte[] data);
    }
}
