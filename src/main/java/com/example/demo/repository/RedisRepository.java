package com.example.demo.repository;

import com.example.demo.expection.ObjectNotFoundException;
import com.example.demo.util.JsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RedisRepository {
    @Autowired
    private RedisTemplate< String, String > template;


    public Collection<String> getAll(){
        Set<String> redisKeys = template.keys("*");
        assert redisKeys != null;
        List<String> keysList = new ArrayList<>(redisKeys);

        return template.opsForValue().multiGet(keysList);
    }

    public String get(String id) throws ObjectNotFoundException {
        if(!template.hasKey(id)){
            throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
        }
        return template.opsForValue().get(id);
    }

    public String save(String plan){
        String id = JsonHelper.getId(plan);
        template.opsForValue().set(id,plan);
        return id;
    }

    public boolean delete(String id) throws ObjectNotFoundException {
        if(!template.hasKey(id)){
            throw new ObjectNotFoundException("Object with id: " + id + " not found in system");
        }

        return template.delete(id);
    }

}
