import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import static utils.Utils.readLine;

public class SocketHttpServer {
    private InetSocketAddress inetSocketAddress;
    /*Save all the path-Handler key,value pairs in a hashmap*/
    private HashMap<String, RequestHandler> registeredPaths = new HashMap<>();

    private SocketHttpServer(InetSocketAddress socketAddress) {
        this.inetSocketAddress = socketAddress;
    }

    private static SocketHttpServer mHttpServer = null;

    public static SocketHttpServer getServer(InetSocketAddress inetSocketAddress) {
        if (mHttpServer == null) {
            mHttpServer = new SocketHttpServer(inetSocketAddress);
        }
        return mHttpServer;
    }

    /*Registers the request and the handler*/
    public void registerEntryPoint(String requestPath, RequestHandler requestHandler) {
        registeredPaths.put(requestPath, requestHandler);
    }

    /*Starts the Server*/
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(inetSocketAddress.getPort());
            System.out.println("Server Started @port >> " + serverSocket.getLocalPort());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Server connected to the client >> " + socket.getRemoteSocketAddress());


                new Thread(() -> {
                    try {

                        /*This contains all the methods for interacting with the client*/
                        SocketConnectionInterface connectionInterface = new SocketConnectionInterface();

                        InputStream dataIn = socket.getInputStream();
                        OutputStream dataOut = socket.getOutputStream();

                        /*Get the request Headers*/
                        getRequestHeaders(dataIn, connectionInterface);
                        String requestedPath = connectionInterface.getRequestPath().trim();
                        System.out.println("RequestedPath >> " + requestedPath);

                        /*After reading the request headers*/
                        connectionInterface.setRequestBody(dataIn);
                        connectionInterface.setResponseBody(dataOut);

                        /*Check for the path in the hashmap*/
                        String[] splitRequestPath = requestedPath.split("/");

                        if (splitRequestPath.length == 0) {
                            if (registeredPaths.containsKey("/")) {
                                registeredPaths.get("/").handleRequest(connectionInterface);
                            } else {
                                throw new RuntimeException("The entry point / is not registered");
                            }
                        } else {
                            String firstPath = "/" + splitRequestPath[1].trim();
                            System.out.println("FirstPath >> " + firstPath);
                            if (registeredPaths.containsKey(firstPath)) {
                                registeredPaths.get(firstPath).handleRequest(connectionInterface);
                            } else {
                                if (registeredPaths.containsKey("/")) {
                                    registeredPaths.get("/").handleRequest(connectionInterface);
                                } else {
                                    throw new RuntimeException("The entry point / is not registered");
                                }
//                                System.err.println("Could not find the key " + firstPath);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("Error in starting the Server >> " + e.getMessage());
        }
    }

    private void getRequestHeaders(InputStream inputStream, SocketConnectionInterface connectionInterface) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        while (true) {
            String line = readLine(inputStream);
            if (line == null) {
                return;
            }
            if (line.trim().length() == 0) {
                connectionInterface.setRequestHeaders(headers);
                break;
            }

            if (line.startsWith("GET") || line.startsWith("POST")) {
                String[] requestLine = line.split(" ");
                String requestMethod = requestLine[0];
                String requestPath = requestLine[1];
                connectionInterface.setRequestMethod(requestMethod);
                connectionInterface.setRequestPath(requestPath);
                continue;
            }
            String[] requestLine = line.split(":");
            String key = requestLine[0];
            String value = requestLine[1];
            headers.put(key, value);
        }
    }
}
