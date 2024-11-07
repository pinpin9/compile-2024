package tools;

public class StrTool {
    public static String getFormat(String string) {
        return string.replace("\n","\\0A");
    }
}
