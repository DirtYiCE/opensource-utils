namespace CoolRMI.Net.Example
{
    public interface IService
    {
        string Echo(string s, int x);
        void ThrowException();
        void InitTimer(ICallback cb, int timeoutMillis);
    }
}
