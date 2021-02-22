package threadpool;

import java.text.MessageFormat;
import java.util.concurrent.*;

/**
 * 功能描述: 自定义的线程池对象
 *
 * @author youyou
 * @date 3/25/20 11:32 AM
 */
public class ThreadPoolBean {

    private final ExecutorService threadPool;
    private final Semaphore semaphore;
    private final int coreThread;
    private final int maxThread;
    private final int taskQueueCapacity;

    public ThreadPoolBean(int coreThread, int maxThread, int taskQueueCapacity) {
        this.coreThread = coreThread;
        this.maxThread = maxThread;
        this.taskQueueCapacity = taskQueueCapacity;
        if (taskQueueCapacity < maxThread) {
            throw new RuntimeException("任务队列的容量必须大于线程池最大线程数");
        }

        this.semaphore = new Semaphore(taskQueueCapacity);
        this.threadPool = new ThreadPoolExecutor(coreThread, maxThread, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(taskQueueCapacity), new ThreadPoolExecutor.AbortPolicy());
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public int getTaskQueueCapacity() {
        return taskQueueCapacity;
    }

    @Override
    public String toString() {
        return MessageFormat.format("a thread pool with {0} core thread, {1} max thread and {2} queue capacity.", coreThread, maxThread, taskQueueCapacity);
    }
}
