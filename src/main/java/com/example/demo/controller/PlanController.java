package com.example.demo.controller;

import com.example.demo.expection.IdExistingException;
import com.example.demo.expection.ObjectNotFoundException;
import com.example.demo.repository.RedisRepository;
import com.example.demo.util.DocumentHelper;
import com.example.demo.util.JsonSchemaValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.google.gson.Gson;


import java.util.*;

@RestController
public class PlanController {

    @Autowired
    private JsonSchemaValidator jsonSchemaValidator;

    @Autowired
    private RedisRepository redisRepository;

    private final String OBJECT_TYPE = "plan";

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/plan")
    public ResponseEntity<Map<String,Object>> getAll() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(redisRepository.getAll());

//        return redisRepository.getAll();
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        try{
            if(!isObjectExist(id)){
                throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
            }
            String documentKey = DocumentHelper.getDocumentKey(id,OBJECT_TYPE);
            Map<String,Object> jsonObject = getDocument(documentKey);
            String jsonString = new Gson().toJson(jsonObject);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonString);
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getReason()
            );
        }
    }

    private boolean isObjectExist(String id) {
        String documentKey = DocumentHelper.getDocumentKey(id,OBJECT_TYPE);
        return redisRepository.contains(documentKey);
    }

    private Map<String,Object> getDocument(String documentKey) throws ObjectNotFoundException {
        String jsonString = redisRepository.get(documentKey);
        Map<String,Object> jsonObject = new Gson().fromJson(jsonString,LinkedHashMap.class);

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,Object> keyValuePair = (Map.Entry) iterator.next();
            String key = keyValuePair.getKey();
            Object value = keyValuePair.getValue();
            if(value instanceof String){
                if(((String) value).contains(DocumentHelper.DOCUMENT_KEY_ID_SEPARATOR)){
                    jsonObject.put(key,getDocument((String) value));
                }
            }
            else if(value instanceof List){
                ArrayList<Object> list = new ArrayList();
                for (Object item: (List) value) {
                    if(item instanceof String){
                        if(((String) item).contains(DocumentHelper.DOCUMENT_KEY_ID_SEPARATOR)) {
                            list.add(getDocument((String) item));
                        }else{
                            list.add(item);
                        }
                    }
                    //list of simply values
                    else {
                        list.add(item);
                    }
                }
                jsonObject.put(keyValuePair.getKey(),list);
            }
        }

        return jsonObject;
    }

    @PutMapping("/plan/{id}")
    public ResponseEntity<String> put(@PathVariable String id, @RequestBody String plan){
        try {
            Map<String,Object> object = new Gson().fromJson(plan,LinkedHashMap.class);
            String documentKey = DocumentHelper.getDocumentKey(id,OBJECT_TYPE);
            if(!isObjectExist(id)){
                throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
            }
            jsonSchemaValidator.validate(plan);
            deleteDocument(documentKey);
            id = UpdateDocument(object);
        } catch (ValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, JsonSchemaValidator.getDetailError(e)
            );
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getReason()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("plan updated, plan id :" + id);
    }

    private String UpdateDocument(Map<String,Object> jsonObject) {
        String documentKey = DocumentHelper.getDocumentKey(jsonObject);
        Map<String,Object> document = new LinkedHashMap<>();

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> keyValuePair = (Map.Entry) iterator.next();
            Object value = keyValuePair.getValue();

            if(Objects.nonNull(value)){
                //property is a object
                if(value instanceof Map){
                    document.put(keyValuePair.getKey(),UpdateDocument((Map)value)) ;
                }
                //property is a list
                else if(value instanceof List){
                    ArrayList<Object> list = new ArrayList();
                    for (Object item: (List) value) {
                        // list of object
                        if(item instanceof Map){
                            list.add(UpdateDocument((Map) item));
                        }
                        //list of simply values
                        else {
                            list.add(item);
                        }
                    }
                    document.put(keyValuePair.getKey(),list);
                }
                //property is simple property
                else {
                    document.put(keyValuePair.getKey(),keyValuePair.getValue());
                }
            }
        }
        redisRepository.update(documentKey,new Gson().toJson(document));

        return documentKey;
    }

    @PostMapping("/plan")
    public ResponseEntity<String> post(@RequestBody String plan){
        String id;
        try {
            Map<String,Object> object = new Gson().fromJson(plan,LinkedHashMap.class);
            if(isObjectExist(object)){
                String documentId = DocumentHelper.getObjectId(object);
                String objectType = DocumentHelper.getObjectType(object);
                throw new IdExistingException("Object: "+ objectType + " with id: " + documentId + " already existed");
            }
            jsonSchemaValidator.validate(plan);
            id = createDocument(object);
        } catch (ValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, JsonSchemaValidator.getDetailError(e)
            );
        } catch (IdExistingException e){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, e.getReason()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("plan created, plan id :" + id);
    }

    private boolean isObjectExist(Map<String,Object> jsonObject) {
        String documentKey = DocumentHelper.getDocumentKey(jsonObject);
        return redisRepository.contains(documentKey);
    }

    private String createDocument(Map<String,Object> jsonObject) throws IdExistingException {
        String documentKey = DocumentHelper.getDocumentKey(jsonObject);
        Map<String,Object> document = new LinkedHashMap<>();

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Object> keyValuePair = (Map.Entry) iterator.next();
            Object value = keyValuePair.getValue();

            if(Objects.nonNull(value)){
                //property is a object
                if(value instanceof Map){
                    document.put(keyValuePair.getKey(),createDocument((Map)value)) ;
                }
                //property is a list
                else if(value instanceof List){
                    ArrayList<Object> list = new ArrayList();
                    for (Object item: (List) value) {
                        // list of object
                        if(item instanceof Map){
                            list.add(createDocument((Map) item));
                        }
                        //list of simply values
                        else {
                            list.add(item);
                        }
                    }
                    document.put(keyValuePair.getKey(),list);
                }
                //property is simple property
                else {
                    document.put(keyValuePair.getKey(),keyValuePair.getValue());
                }
            }
        }
        redisRepository.save(documentKey,new Gson().toJson(document));

        return documentKey;
    }

    @PatchMapping("/plan/{id}")
    public ResponseEntity<String> patch(@PathVariable String id,@RequestBody String updateObjectString) {
        try {
            String mainObjectDocumentKey = DocumentHelper.getDocumentKey(id,OBJECT_TYPE);
            Map<String,Object> mainObject = getDocument(mainObjectDocumentKey);
            Map<String,Object> updateObject = new Gson().fromJson(updateObjectString,LinkedHashMap.class);

            Map<String,Object> result = DocumentHelper.deepMerge(mainObject,updateObject);
            String resultString = new Gson().toJson(result);
            jsonSchemaValidator.validate(resultString);

            deleteDocument(mainObjectDocumentKey);
            UpdateDocument(result);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(resultString);
        }catch (ObjectNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getReason()
            );
        }catch (ValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, JsonSchemaValidator.getDetailError(e)
            );
        }catch (Exception e){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()
            );
        }
    }


    @DeleteMapping("/plan/{id}")
    public Boolean delete(@PathVariable String id) {
        try {
            if(!isObjectExist(id)){
                throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
            }
            String documentKey = DocumentHelper.getDocumentKey(id,OBJECT_TYPE);
            return deleteDocument(documentKey);
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getReason()
            );
        }

    }

    private Boolean deleteDocument(String documentKey) throws ObjectNotFoundException {
        String jsonString = redisRepository.get(documentKey);
        Map<String, Object> jsonObject = new Gson().fromJson(jsonString, LinkedHashMap.class);

        Iterator iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> keyValuePair = (Map.Entry) iterator.next();
            Object value = keyValuePair.getValue();

            if (Objects.nonNull(value)) {
                if(value instanceof String){
                    if(((String) value).contains(DocumentHelper.DOCUMENT_KEY_ID_SEPARATOR)){
                        deleteDocument((String) value);
                    }
                }else if( value instanceof List){
                    for(Object item: (List) value){
                        if(item instanceof String){
                            if(((String) item).contains(DocumentHelper.DOCUMENT_KEY_ID_SEPARATOR)){
                                deleteDocument((String) item);
                            }
                        }
                    }
                }

            }
        }
        redisRepository.delete(documentKey);
        return true;
    }


}
