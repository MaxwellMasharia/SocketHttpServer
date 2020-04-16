import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class SocketConnectionInterface {
    String requestMethod;
    String requestPath;
    private HashMap<String, String> requestHeaders;
    private HashMap<String, String> responseHeaders;
    private InputStream requestBody;
    private OutputStream responseBody;
    boolean areResponseHeadersSent = false;

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public InputStream getRequestBody() {
        return requestBody;
    }

    public OutputStream getResponseBody() {
        if (!areResponseHeadersSent) {
            throw new IllegalStateException("Response headers not sent yet");
        }
        return responseBody;
    }

    public void setRequestBody(InputStream requestBody) {
        this.requestBody = requestBody;
    }

    public void setResponseBody(OutputStream responseBody) {
        this.responseBody = responseBody;
    }

    public void setResponseHeaders(HashMap<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestHeaders(HashMap<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void sendResponseHeaders(int contentLength, HashMap<String, String> responseHeaders) {
        PrintWriter printWriter = new PrintWriter(responseBody);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Length : " + contentLength);
        responseHeaders.forEach((key, value) -> printWriter.println(key + " : " + value));
        printWriter.println();
        printWriter.flush();

        areResponseHeadersSent = true;
    }

}
