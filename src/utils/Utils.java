package utils;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static String readLine(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstLine = true;
        while (true) {
            int readByte = inputStream.read();
            if (readByte == -1) {
                if (isFirstLine) {
                    return null;
                } else {
                    return stringBuilder.toString();
                }
            } else if (readByte == 10) {
                return stringBuilder.toString();
            }
            stringBuilder.append((char) readByte);
            isFirstLine = false;
        }
    }
}
