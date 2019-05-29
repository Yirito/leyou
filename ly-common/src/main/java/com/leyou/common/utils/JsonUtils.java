package com.leyou.common.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User {
        String name;
        Integer age;
    }

    /**
     * 其实可以使用GSon或者fastJson
     *
     * @param args
     */
    public static void main(String[] args) {
        User jack = new User("Jack", 21);
        //序列化
        //String json = toString(jack);
        //System.out.println("json = " + json);

        //反序列化
        //User user = toBean(json, User.class);
        //System.out.println("user = " + user);

        //集合
        //String json = "[20,-10,5,15]";
        //List<Integer> integers = toList(json, Integer.class);
        //System.out.println("integers = " + integers);

        //map
        //String json = "{\"name\":\"Jack\",\"age\":\"21\"}";
        //Map<String, String> stringStringMap = toMap(json, String.class, String.class);
        //System.out.println("stringStringMap = " + stringStringMap);

        //复杂的序列化
        String json = "[{\"name\":\"Jack\",\"age\":\"21\"},{\"name\":\"Rose\",\"age\":\"18\"},{\"name\":\"XI\",\"age\":\"24\"}]";
        List<Map<String, String>> maps = nativeRead(json, new TypeReference<List<Map<String, String>>>() {
        });
        for (Map<String, String> map : maps) {
            System.out.println("map = " + map);
        }
    }
}
