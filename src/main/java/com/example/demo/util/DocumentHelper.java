package com.example.demo.util;

import java.util.*;

public class DocumentHelper {

    //kPlainTextPlaceholder
    public static String DOCUMENT_KEY_ID_SEPARATOR = new String(Character.toChars(0x200b));

    public static String getDocumentKey(Map<String,Object> jsonObject){
        String objectId = (String) jsonObject.get("objectId");
        String objectType = (String) jsonObject.get("objectType");
        return objectType + DOCUMENT_KEY_ID_SEPARATOR +objectId;
    }

    public static String getDocumentKey(String objectId,String objectType){
        return objectType + DOCUMENT_KEY_ID_SEPARATOR +objectId;
    }

    public static String getObjectId(Map<String,Object> jsonObject){
        return (String) jsonObject.get("objectId");
    }

    public static String getObjectType(Map<String,Object> jsonObject){
        return (String) jsonObject.get("objectType");
    }

    public static Map<String,Object> deepMerge(Map<String,Object> mainObject,Map<String,Object> updateObject){

        for (Map.Entry<String, Object> stringObjectEntry : updateObject.entrySet()) {
            String updateKey = ((Map.Entry<String, Object>) (Map.Entry) stringObjectEntry).getKey();
            Object updateValue = updateObject.get(updateKey);
            Object mainValue = mainObject.get(updateKey);

            if (Objects.nonNull(updateValue)) {
                if (updateValue instanceof List && mainValue instanceof List) {
                    for (Object updateItem : (List) updateValue) {
                        if (updateItem instanceof Map) {
                            int index = ListContainsId((String) ((Map) updateItem).get("objectId"), (List) mainValue);
                            if (index != -1) {
                                ((List) mainValue).set(index, deepMerge((Map) ((List) mainValue).get(index), (Map) updateItem));
                            } else {
                                ((List) mainValue).add(updateItem);
                            }
                        }
                    }
                } else if (updateValue instanceof Map && mainValue instanceof Map) {
                    deepMerge((Map<String, Object>) mainValue, (Map<String, Object>) updateValue);
                } else {
                    mainObject.put(updateKey, updateValue);
                }
            }

        }
        return mainObject;
    }

    private static int ListContainsId(String id,List<Map<String,Object>> list){
        for (int i = 0; i < list.size();i++){
            String listItemId = (String) list.get(i).get("objectId");
            if(listItemId.equals(id) ){
                return i;
            }
        }
        return -1;
    }
}
