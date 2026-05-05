package game.common.util;

import java.util.HashMap;
import java.util.Map;

public class CommonUtil {
    public static <V> Map<String, V> toStringKeyMap(Map<?, V> source) {
        Map<String, V> result = new HashMap<>();
        if (source == null) {
            return result;
        }

        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }
}
