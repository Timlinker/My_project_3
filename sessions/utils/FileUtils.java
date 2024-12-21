package sessions.utils;

import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static JSONArray readJsonArray(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        return new JSONArray(content);
    }

    public static void writeJsonArray(String filePath, JSONArray jsonArray) throws IOException {
        Files.writeString(Path.of(filePath), jsonArray.toString(2));
    }
}