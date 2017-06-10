using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace CoolRMI.Net
{
    public class SingleThreadTaskScheduler : TaskScheduler, IDisposable
    {
        private readonly BlockingCollection<Task> tasks =
            new BlockingCollection<Task>();

        public SingleThreadTaskScheduler()
        {
            var thread =
                new Thread(ThreadMethod) {Name = "SingleThreadTaskScheduler"};
            thread.Start();
        }

        private void ThreadMethod()
        {
            Task item;
            while (tasks.TryTake(out item, -1))
                TryExecuteTask(item);
        }

        protected override void QueueTask(Task task)
        {
            tasks.Add(task);
        }

        protected override bool TryExecuteTaskInline(Task task,
            bool taskWasPreviouslyQueued)
        {
            return false;
        }

        protected override IEnumerable<Task> GetScheduledTasks()
        {
            return tasks;
        }

        public void Dispose()
        {
            tasks.CompleteAdding();
        }
    }
}
