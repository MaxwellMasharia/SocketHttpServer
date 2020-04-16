import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) {
        SocketHttpServer httpServer = SocketHttpServer.getServer(new InetSocketAddress(4041));
        httpServer.registerEntryPoint("/", connectionInterface -> {
            connectionInterface.getRequestHeaders().forEach((key, value) -> System.out.println(key + " : " + value));
            connectionInterface.sendResponseHeaders(0, new HashMap<>());
            try {
                connectionInterface.getRequestBody().close();
                connectionInterface.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        httpServer.start();
    }
}
