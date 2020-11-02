package com.example.demo.controller;

import com.example.demo.expection.IdExistingException;
import com.example.demo.expection.ObjectNotFoundException;
import com.example.demo.repository.RedisRepository;
import com.example.demo.util.JsonSchemaValidator;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class PlanController {

    @Autowired
    private JsonSchemaValidator jsonSchemaValidator;

    @Autowired
    private RedisRepository redisRepository;

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/plan")
    public ResponseEntity<Collection<String>> getAll() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(redisRepository.getAll());

//        return redisRepository.getAll();
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        try{
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(redisRepository.get(id));
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getReason()
            );
        }
    }

    @PostMapping("/plan")
    public ResponseEntity<String> put(@RequestBody String plan){
        String id;
        try {
            jsonSchemaValidator.validate(plan);
            id = redisRepository.save(plan);
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


//    @PatchMapping("/plan")
//    public ResponseEntity<String> patch(@RequestBody String plan) {
//        HashMap object = new HashMap();
//        JSONParser parser = new JSONParser();
//        try{
//            object = (HashMap) parser.parse(plan);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        String objectId = (String) object.get("objectId");
//        String objectType = (String) object.get("objectType");
//        String objectKey = objectType + objectId;
//
//        Iterator iterator = object.entrySet().iterator();
//        while (iterator.hasNext()){
//            Map.Entry keyValuePair = (Map.Entry) iterator.next();
//            String Key = (String) keyValuePair.getKey();
//            Object value = keyValuePair.getValue();
//
//            if(value != null){
//                // value is a object
//                if(value instanceof Map){
//                    //add a object link in to map
//                }
//            }
//        }
//
//
//
//
//    }

    @DeleteMapping("/plan/{id}")
    public Boolean delete(@PathVariable String id) {
        try {
            return redisRepository.delete(id);
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getReason()
            );
        }

    }


}
