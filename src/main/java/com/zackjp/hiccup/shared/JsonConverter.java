package com.zackjp.hiccup.shared;

public interface JsonConverter {
    String toJson(Object object);
    <T> T fromJson(String json, Class<T> aClass);
}
