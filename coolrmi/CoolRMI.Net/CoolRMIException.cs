using System;
using System.Runtime.Serialization;

namespace CoolRMI.Net
{
    public class CoolRMIException : Exception
    {
        public CoolRMIException()
        {
        }

        public CoolRMIException(string message) : base(message)
        {
        }

        protected CoolRMIException(SerializationInfo info,
            StreamingContext context) : base(info, context)
        {
        }

        public CoolRMIException(string message, Exception innerException) :
            base(message, innerException)
        {
        }
    }

    public class CoolRMITimeoutException : CoolRMIException
    {
        public CoolRMITimeoutException()
        {
        }

        public CoolRMITimeoutException(string message) : base(message)
        {
        }

        protected CoolRMITimeoutException(SerializationInfo info, StreamingContext context) : base(info, context)
        {
        }

        public CoolRMITimeoutException(string message, Exception innerException) : base(message, innerException)
        {
        }
    }
}
