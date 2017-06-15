namespace CoolRMI.Net.Example
{
    public interface IService
    {
        string echo(string s, int x);
        void exceptionExample();
        void initTimer(ICallback cb, long timeoutMillis);
    }
}
