package com.example.demo.util;

import org.json.JSONObject;

public class JsonHelper {

    public static String getId(String jsonString){
        JSONObject jsonObject = new JSONObject(jsonString);
        return (String) jsonObject.get("objectId");
    }

    public static String getCondenseJsonString(String jsonString){
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.toString();
    }
}
