package com.example.demo.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlanRepository {
    Map<String,String> store = new HashMap<>();

    public Collection<String> getAll(){
        return store.values();
    }

    public String get(String id){
        return store.get(id);
    }

    public String save(String plan){
        String uuid = UUID.randomUUID().toString();
        store.put(uuid,plan);
        return uuid;
    }

    public String  delete(String id){
        return store.remove(id);
    }
}
