package com.example.demo.queue.event;

import java.util.Map;

public class DocumentIndexEvent {
    private Map<String, Object> jsonObject;
    private boolean isDelete;

    public DocumentIndexEvent(Map<String, Object> jsonObject, boolean isDelete){
        this.jsonObject = jsonObject;
        this.isDelete = isDelete;
    }


    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public Map<String, Object> getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(Map<String, Object> jsonObject) {
        this.jsonObject = jsonObject;
    }
}
