package game.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;

public class JsonUtil {

    private JsonUtil() {}

    /**
     * 对象 → JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        return JSON.toJSONString(obj);
    }

    /**
     * JSON字符串 → 对象
     */
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        return JSON.parseObject(json, clazz);
    }

    /**
     * JSON → 泛型（List / Map）
     */
    public static <T> T parse(String json, TypeReference<T> type) {
        if (json == null || json.isEmpty()) return null;
        return JSON.parseObject(json, type);
    }

    /**
     * JSON → JSONObject
     */
    public static JSONObject parseObj(String json) {
        if (json == null || json.isEmpty()) return null;
        return JSON.parseObject(json);
    }

    /**
     * JSON → JSONArray
     */
    public static JSONArray parseArr(String json) {
        if (json == null || json.isEmpty()) return null;
        return JSON.parseArray(json);
    }

    /**
     * JSONObject → 对象
     */
    public static <T> T objToBean(JSONObject obj, Class<T> clazz) {
        if (obj == null) return null;
        return obj.to(clazz);
    }

    /**
     * 对象 → JSONObject
     */
    public static JSONObject beanToObj(Object obj) {
        if (obj == null) return null;
        return (JSONObject) JSON.toJSON(obj);
    }

    /**
     * 安全获取字段
     */
    public static String getString(JSONObject obj, String key) {
        return obj != null ? obj.getString(key) : null;
    }

    public static int getInt(JSONObject obj, String key) {
        return obj != null && obj.get(key) != null ? obj.getIntValue(key) : 0;
    }

    public static long getLong(JSONObject obj, String key) {
        return obj != null && obj.get(key) != null ? obj.getLongValue(key) : 0L;
    }

    public static boolean getBool(JSONObject obj, String key) {
        return obj != null && obj.get(key) != null && obj.getBooleanValue(key);
    }
}