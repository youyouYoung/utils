package threadpool;

import java.util.List;

/**
 * 功能描述: 业务中的任务和任务的执行方法
 *
 * @author youyou
 * @date 3/25/20 11:58 AM
 */
public interface BusinessCommand<E> {

    /**
     * 功能描述: 从外部获取任务的集合
     *
     * @return 待处理任务的集合
     * @author youyou
     * @date 3/25/20 12:04 PM
     */
    List<E> getTaskList();

    /**
     * 功能描述: 将{@code getTaskList}获得的任务集合分为多少份
     *
     * @return 份数
     * @author youyou
     * @date 3/25/20 12:04 PM
     */
    default int numberOfPieces() {
        return 5;
    }

    /**
     * 功能描述: 对每个任务的消费方法
     *
     * @param e 任务对象
     * @author youyou
     * @date 3/25/20 12:06 PM
     */
    void execute(E e);

    /**
     * 功能描述: 获取当前任务内容的描述
     *
     * @return 任务的描述
     * @author youyou
     * @date 3/25/20 12:23 PM
     */
    String description();

    /**
     * 功能描述: 任务的唯一标识, 作为redis key的组成部分
     *
     * @return 任务的唯一标识
     * @author youyou
     * @date 3/25/20 2:43 PM
     */
    String uniqueMark();
}
