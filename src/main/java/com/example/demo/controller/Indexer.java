package com.example.demo.controller;

import com.example.demo.util.DocumentHelper;
import com.google.gson.Gson;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
public class Indexer {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private PlanController planController;

    private Gson gson = new Gson();

    @PutMapping("/indexer/{id}")
    public ResponseEntity<String> indexDocument(@PathVariable String id) {
        ResponseEntity<String> responseEntity = planController.get(id, null);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String jsonString = responseEntity.getBody();
            Map<String, Object> jsonObject = gson.fromJson(jsonString, LinkedHashMap.class);
            indexDocument(jsonObject);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("request document with id" + id + "not found");
        }

        return ResponseEntity.ok().body("indexer here");
    }

    public void indexDocument(Map<String, Object> jsonObject){
        String planId = DocumentHelper.getDocumentKey(jsonObject);
        indexDocument(jsonObject, "plan", null, planId);
    }

    private void indexDocument(Map<String, Object> jsonObject, String objectType, String parentId
            , String planId) {
        String documentKey = DocumentHelper.getDocumentKey(jsonObject);
        Map<String, Object> document = new LinkedHashMap<>();

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> keyValuePair = (Map.Entry) iterator.next();
            String key = keyValuePair.getKey();
            Object value = keyValuePair.getValue();

            if (Objects.nonNull(value)) {
                //property is a object
                if (value instanceof Map) {
                    indexDocument((Map<String, Object>) value, key, documentKey, planId);
                } else if (value instanceof List) {
                    for (Object item : (List) value) {
                        // list of object
                        if (item instanceof Map) {
                            indexDocument((Map<String, Object>) item, key, documentKey, planId);
                        }
                    }
                } else {
                    document.put(keyValuePair.getKey(), keyValuePair.getValue());
                }
            }
        }
        document.put("plan_service", addJoinRelationship(objectType, Optional.ofNullable(parentId)));

        IndexRequest indexRequest = new IndexRequest("plan")
                .id(documentKey).source(document).routing(planId);
        try {
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private Map<String, Object> addJoinRelationship(String objectType, Optional<String> parentId) {
        Map<String, Object> document = new LinkedHashMap<>();
        if (parentId.isPresent()) {
            document.put("name", objectType);
            document.put("parent", parentId.get());
        } else {
            document.put("name", objectType);
        }
        return document;
    }


    @DeleteMapping("/indexer/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable String id) {
        ResponseEntity<String> responseEntity = planController.get(id, null);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            String jsonString = responseEntity.getBody();
            Map<String, Object> jsonObject = new Gson().fromJson(jsonString, LinkedHashMap.class);
            deleteDocument(jsonObject);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("request document with id" + id + "not found");
        }

        return ResponseEntity.ok().body("indexer here");
    }

    public void deleteDocument(Map<String, Object> jsonObject) {
        String documentKey = DocumentHelper.getDocumentKey(jsonObject);
        Map<String, Object> document = new LinkedHashMap<>();

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> keyValuePair = (Map.Entry) iterator.next();
            Object value = keyValuePair.getValue();

            if (Objects.nonNull(value)) {
                //property is a object
                if (value instanceof Map) {
                    deleteDocument((Map<String, Object>) value);
                } else if (value instanceof List) {
                    for (Object item : (List) value) {
                        // list of object
                        if (item instanceof Map) {
                            deleteDocument((Map<String, Object>) item);
                        }
                    }
                }
            }

            DeleteRequest request = new DeleteRequest("plan", documentKey);
            try {
                DeleteResponse deleteResponse = restHighLevelClient.delete(
                        request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }


    }
}
