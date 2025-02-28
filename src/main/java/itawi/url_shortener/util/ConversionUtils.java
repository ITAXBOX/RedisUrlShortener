package itawi.url_shortener.util;

public final class ConversionUtils {

    private static final String BASE62_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private ConversionUtils() {
    }

    public static String convertToBase62(Long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.insert(0, BASE62_CHARS.charAt((int) (id % 62)));
            id /= 62;
        }
        return sb.toString();
    }
}
