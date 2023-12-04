/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.sampleredisapplication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author raghu.v
 */
@Service
public class StreamService {
    
     @Value("${redis.hashkey}")
    private String hashKey;
    
    
      @Autowired
    private RedisTemplate<String, String> redisTemplate;
      
      
       private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
     public boolean doesSessionExists(String sessionId) {
        boolean isKeyinHashSessions = redisTemplate.opsForHash().hasKey(hashKey, sessionId);
        if (isKeyinHashSessions) {
            logger.info("Session with session id {} exists in hash set {} ?????? {}", sessionId, hashKey, isKeyinHashSessions);
            return true;
        }

        return false;
    }
    
}
