package redis;


/**
 * Description: test class for CodisConnector
 *
 * @author youyou
 * @date 3/5/21 11:13 AM
 */
public class CodisConnectorTest {

    public static void main(String[] args) {
        setnx();
    }

    private static void setnx() {
        CodisConnector.setnx("key", 100, "vale");
    }
}