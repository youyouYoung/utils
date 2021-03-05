package property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Description: the class to operate local custom properties.
 *
 * @author youyou
 * @date 3/5/21 10:54 AM
 */
public class CustomProperties {
    private static Properties prop;
    static {
        String file = "properties";
        prop = new Properties();

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream(file);
            prop.load(stream);
        } catch (IOException e) {
            prop = null;
        }
    }

    public static Properties getProperties() {
        return prop;
    }
}
