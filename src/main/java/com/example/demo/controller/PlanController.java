package com.example.demo.controller;

import com.example.demo.expection.ObjectNotFoundException;
import com.example.demo.repository.RedisRepository;
import com.example.demo.util.JsonSchemaValidator;
import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Collection<String> getAll() {
        return redisRepository.getAll();
    }

    @GetMapping("/plan/{id}")
    public String get(@PathVariable String id) {
        try{
            return redisRepository.get(id);
        } catch (ObjectNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getReason()
            );
        }
    }

    @PutMapping("/plan")
    public ResponseEntity<String> put(@RequestBody String plan){
        String id;
        try {
            jsonSchemaValidator.validate(plan);
            id = redisRepository.save(plan);
        } catch (ValidationException e) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_FAILED, "Invalid Json object"
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("plan created, plan id :" + id);
    }

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
