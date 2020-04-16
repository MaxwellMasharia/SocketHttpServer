import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class Test {
    public static void main(String[] args) {
        SocketHttpServer httpServer = SocketHttpServer.getServer(new InetSocketAddress(4041));
        httpServer.registerEntryPoint("/", connectionInterface -> {
            System.out.println("Default Called");
            connectionInterface.getRequestHeaders().forEach((key, value) -> System.out.println(key + " : " + value));
            System.out.println();

            String requestedFile = connectionInterface.getRequestPath();
            if (requestedFile.equals("/")) {
                requestedFile = "src/web/index.html";
            } else {
                requestedFile = "src/web" + requestedFile;
            }

            try {
                File file = new File(requestedFile);
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                int readBytes = fileInputStream.read(data);
                System.out.println("Read " + readBytes + " bytes from the file >> " + requestedFile);
                fileInputStream.close();

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "text/html");
                connectionInterface.sendResponseHeaders((int) file.length(), headers);

                OutputStream outputStream = connectionInterface.getResponseBody();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                bufferedOutputStream.write(data);
                bufferedOutputStream.flush();

                connectionInterface.getRequestBody().close();
                connectionInterface.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        httpServer.start();
    }
}
