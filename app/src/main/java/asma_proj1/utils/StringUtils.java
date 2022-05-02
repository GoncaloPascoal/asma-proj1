package asma_proj1.utils;

public class StringUtils {
    public static final String RED = "\033[1;31m",
        GREEN = "\033[1;32m",
        YELLOW = "\033[1;33m",
        BLUE = "\033[1;34m";

    private static final String RESET = "\u001B[0m";

    private StringUtils() {}

    public static String colorize(String str, String color) {
        return color + str + RESET;
    }

    public static void logError(String str) {
        System.err.println(colorize(str, RED));
    }
}
