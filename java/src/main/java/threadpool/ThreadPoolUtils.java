package threadpool;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import logger.Logger;
import org.apache.commons.collections.CollectionUtils;
import redis.CodisConnector;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述: 线程池工具类
 *
 * @author youyou
 * @date 3/25/20 12:08 PM
 */
public class ThreadPoolUtils {
    private static final Logger LOGGER = Logger.getLogger(ThreadPoolUtils.class, "threadpool");
    private static final String LOCK_KEY_TEMPLATE = "personal:youyou:young:command:execute:{0}";

    /**
     * 功能描述: 使用传入的线程池对象{@param pool}执行指定的业务内容{@param command}. 具体的执行方式有业务内容的{@code execute()} 方式指定.
     *
     * 该方法通过 command 对象获取任务的集合, 将任务集合划分为多个子集合. 子集合个数由 command.numberOfPieces 指定. 使用新的线程执行子集合中的每个任务.
     *
     * 直到 command 中获取不到新的元素为止.
     *
     * @param pool 线程池对象
     * @param command 业务内容
     * @author youyou
     * @date 3/25/20 11:51 PM
     */
    public static <E> void taskExecute(ThreadPoolBean pool, BusinessCommand<E> command) {
        final ExecutorService threadPool = pool.getThreadPool();
        final Semaphore semaphore = pool.getSemaphore();
        final String lockKey = MessageFormat.format(LOCK_KEY_TEMPLATE, command.uniqueMark());

        try {
            semaphore.acquire();
            threadPool.execute(() -> {
                try {
                    LOGGER.info("ThreadPoolUtils.taskExecute. execute task started. task:{}", command.description());
                    do {
                        // 获取分布式锁
                        if (CodisConnector.setnx(lockKey, 60 * 2, "true") < 1) {
                            return;
                        }

                        List<E> elements = command.getTaskList();
                        boolean hasMoreElements = CollectionUtils.isNotEmpty(elements);
                        LOGGER.info("ThreadPoolUtils.taskExecute. get new element list from command. has more elements:{}", hasMoreElements);
                        // 获取到数据之后就可以解锁, 使得其他进程开始执行
                        CodisConnector.del(lockKey);

                        // 如果没有新的任务, 跳出循环
                        if (!hasMoreElements) {
                            break;
                        }

                        // 将大的集合分为多个子集合
                        int pieces = command.numberOfPieces();
                        for (List<E> subElements : Lists.partition(elements, pieces)) {
                            executeSubList(threadPool, semaphore, subElements, command);
                        }
                    } while (true);

                    LOGGER.info("ThreadPoolUtils.taskExecute. all elements has been submitted. task:{}", command.description());
                } catch (Exception e) {
                    LOGGER.info("ThreadPoolUtils.taskExecute. get elements from command object error. command:{}", command, e);
                } finally {
                    threadPool.shutdown();
                    semaphore.release();
                }
            });
        } catch (InterruptedException e) {
            LOGGER.error("ThreadPoolUtils.taskExecute. the task is interrupted. task:{}", command, e);
        }
    }

    /**
     * 功能描述: 使用一个新的线程处理子集合{@param subElements}中的每个元素
     *
     * @param threadPool 线程池对象
     * @param semaphore 信号量, 用以表示线程池的可用资源数
     * @param subElements 任务子集合
     * @param command 业务对象
     * @author youyou
     * @date 3/25/20 11:53 PM
     */
    private static <E> void executeSubList(ExecutorService threadPool, Semaphore semaphore, List<E> subElements, BusinessCommand<E> command) {
        try {
            semaphore.acquire();
            threadPool.execute(() -> {
                try {
                    for (E element : subElements) {
                        try {
                            LOGGER.info("ThreadPoolUtils.executeSubList begin to execute element of sublist. command:{}, element:{}", command, JSON.toJSONString(element));
                            command.execute(element);
                        } catch (Exception e) {
                            LOGGER.info("ThreadPoolUtils.executeSubList execute command of element error. command:{}, element:{}", command, JSON.toJSONString(element), e);
                        }
                    }
                } finally {
                    semaphore.release();
                }
            });
        } catch (InterruptedException e) {
            LOGGER.error("ThreadPoolUtils.executeSubList the task to execute sublist is interrupted. task:{}, subElements:{}", command, JSON.toJSONString(subElements), e);
        }
    }

    /**
     * 功能描述: 判断当前线程池是否执行完成
     *
     * @param pool 线程池对象
     * @return true表示已完成
     * @author youyou
     * @date 3/25/20 11:59 PM
     */
    public static boolean isFinished(ThreadPoolBean pool) {
        try {
            final Semaphore semaphore = pool.getSemaphore();
            final int capacity = pool.getTaskQueueCapacity();
            return semaphore.tryAcquire(capacity, 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("ThreadPoolUtils.isFinished judging task finished is interrupted.", e);
            return false;
        }
    }

    /**
     * 功能描述: 等待直到线程池中所有任务执行完成
     *
     * @param pool 线程池对象
     * @author youyou
     * @date 3/26/20 12:00 AM
     */
    public static void waitUntilFinished(ThreadPoolBean pool) {
        final Semaphore semaphore = pool.getSemaphore();
        final int capacity = pool.getTaskQueueCapacity();

        try {
            while (semaphore.availablePermits() != capacity) {
                Thread.sleep(2000L);
            }
        } catch (InterruptedException e) {
            LOGGER.error("ThreadPoolUtils.waitUntilFinished waiting task finished is interrupted.", e);
        }
    }

    /**
     * 功能描述: 获取一个指定大小的线程池对象
     *
     * @param coreThread 核心线程数
     * @param maxThread 最大线程数
     * @param taskQueueCapacity 任务队列大小
     * @return 线程池对象
     * @author youyou
     * @date 3/26/20 12:00 AM
     */
    public static ThreadPoolBean getFixedPool(int coreThread, int maxThread, int taskQueueCapacity) {
        if (coreThread < 2) {
            throw new RuntimeException("不支持单线程的线程池");
        }

        if (maxThread < coreThread) {
            throw new RuntimeException("线程池配置有误");
        }
        return new ThreadPoolBean(coreThread, maxThread, taskQueueCapacity);
    }
}
