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

        httpServer.registerEntryPoint("/uploads", connectionInterface -> {
            HashMap<String, String> headers = connectionInterface.getRequestHeaders();
            try {
                uploadFile(headers, connectionInterface.getRequestBody());
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    private static void uploadFile(HashMap<String, String> headers, InputStream inputStream) throws IOException {
        BufferedInputStream dataIn = new BufferedInputStream(inputStream);
        String fileName = headers.get("FileName").trim();
        String fileSize = headers.get("Content-Length").trim();

        System.out.println("RequestedFile >> " + fileName);
        System.out.println("RequestedFileSize >> " + fileSize);

        File file = new File("src/uploads/" + fileName);
        boolean wasFileCreated = file.createNewFile();
        if (wasFileCreated) {
            System.out.println("The file >> " + file.getAbsolutePath() + " was created successfully");
        } else {
            System.err.println("Error in creating the file >> " + fileName);
            return;
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int totalReadBytes = 0;
        while (true) {
            int readByte = dataIn.read();
            totalReadBytes++;
            fileOutputStream.write(readByte);
            if (totalReadBytes == Integer.parseInt(fileSize)) {
                System.out.println("Done Reading >> " + totalReadBytes + " bytes of file of size " + fileSize);
                break;
            }
        }
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
