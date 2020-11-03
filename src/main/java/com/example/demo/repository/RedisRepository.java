package com.example.demo.repository;

import com.example.demo.expection.IdExistingException;
import com.example.demo.expection.ObjectNotFoundException;
import com.example.demo.util.JsonHelper;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RedisRepository {
    @Autowired
    private RedisTemplate< String, String > template;


//    public Collection<String> getAll(){
//        Set<String> redisKeys = template.keys("*");
//        assert redisKeys != null;
//        List<String> keysList = new ArrayList<>(redisKeys);
//
//        return template.opsForValue().multiGet(keysList);
//    }

    public Map<String,Object> getAll(){
        Map<String,Object> map = new LinkedHashMap<>();
        Set<String> redisKeys = template.keys("*");
        assert redisKeys != null;
        for (String key : redisKeys) {
            String value = template.opsForValue().get(key);
            Map<String,Object> jsonObject = new Gson().fromJson(value,LinkedHashMap.class);
            map.put(key,jsonObject);
        }
        return map;
    }

    public String get(String documentKey) throws ObjectNotFoundException {
        if(!template.hasKey(documentKey)){
            throw new ObjectNotFoundException("Object with id: " + documentKey + " not found in system");
        }
        return template.opsForValue().get(documentKey);
    }

    public String save(String plan) throws IdExistingException {
        String id = JsonHelper.getId(plan);
        if(template.hasKey(id)){
            throw new IdExistingException("Object with id: " + id + " already existed");
        }
        template.opsForValue().set(id,JsonHelper.getCondenseJsonString(plan));
        return id;
    }

    public String save(String documentKey,String plan) throws IdExistingException {
        if(template.hasKey(documentKey)){
            throw new IdExistingException("Object with id: " + documentKey + " already existed");
        }
        template.opsForValue().set(documentKey,JsonHelper.getCondenseJsonString(plan));
        return documentKey;
    }

    public String update(String documentKey,String plan){
        template.opsForValue().set(documentKey,JsonHelper.getCondenseJsonString(plan));
        return documentKey;
    }


    public boolean delete(String id) throws ObjectNotFoundException {
        if(!template.hasKey(id)){
            throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
        }

        return template.delete(id);
    }

    public boolean contains(String id)  {
        return template.hasKey(id);
    }

}
