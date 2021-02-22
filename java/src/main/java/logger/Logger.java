package logger;

/**
 * 功能描述: the log decorator
 *
 * @author youyou
 * @date 2/22/21 4:56 PM
 */
public class Logger {
    static public Logger getLogger(Class<?> clazz, String module) {
        return new Logger(clazz, module);
    }

    private Logger(Class<?> clazz, String module) {

    }

    public void info(String msg, Object... args) {

    }

    public void error(String msg, Object... args) {

    }

    public void error(Throwable e) {}
}
