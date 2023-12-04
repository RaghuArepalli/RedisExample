/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.sampleredisapplication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.example.sampleredisapplication.service.StreamService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author raghu.v
 */
@RestController
@RequestMapping("/redishash")
public class Controller {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${redis.hashkey}")
    private String hashKey;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private StreamService streamService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(method = RequestMethod.POST, path = "/postredisdata", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity postCallbackResponse(@RequestBody String requestBody) {
        int res = redisTemplate.getConnectionFactory().getConnection().ping().hashCode();
        logger.info("@@@@@@@@@{}", res);
        Map<String, Object> response = new HashMap<>();
//            logger.debug("Agent type <-> {} :::::::::: kookooNumber <-> {} :::::::::: timeStamap <-> {}", responseType, kookooNumber, timeStamp);
        response.put("status", "fail");
        response.put("Message", "somethingwentwrong");
        logger.info("json request body{}", requestBody);
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Assuming requestBody is a single JSON object
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            JsonNode dataNode = jsonNode.get("data");
            String ucid = dataNode.get("ucid").asText();
            logger.info("ucid: " + ucid);

            boolean isSessionExist = streamService.doesSessionExists(ucid);

            if (!isSessionExist) {
                // Convert the JSON object to a string before saving it to Redis
                redisTemplate.opsForHash().putIfAbsent(hashKey, ucid, jsonNode.toString());
            } else {
                HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
                logger.info("hash values {}", hashOperations.get(hashKey, ucid));
                String json = hashOperations.get(hashKey, ucid);
                // Convert the existing JSON string to a JsonNode
                JsonNode existingJsonNode = objectMapper.readTree(json);
                // Concatenate the new JSON object to the existing JSON array
                ArrayNode jsonArray = objectMapper.createArrayNode();
                jsonArray.add(existingJsonNode);
                jsonArray.add(jsonNode);

                // Update the Redis hash with the new concatenated JSON array
                hashOperations.put(hashKey, ucid, jsonArray.toString());
            }

            // Handle the response as needed
        } catch (JsonProcessingException e) {
            // Handle the exception
        }

        response.put("status", "suceess");
        response.put("Messag", "sucess");
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/getredisdata", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getCallBackResponse(@RequestParam(required = true) String ucid) throws JsonProcessingException {
        Map<String, Object> response = new HashMap<>();
        logger.info("ucid:::::::{}", ucid);
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            String concatenatedString = hashOperations.get(hashKey, ucid);

            if (concatenatedString == null || concatenatedString.isEmpty() || concatenatedString.equalsIgnoreCase("")) {
                response.put("status", "suceess");
                response.put("Messag", "nodata is present");
                response.put("data", ucid);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                // Attempt to deserialize as a JSON array
                List<JsonNode> jsonArray = objectMapper.readValue(concatenatedString, new TypeReference<List<JsonNode>>() {
                });

                // If successful, treat it as an array
                response.put("status", "suceess");
                response.put("Messag", "sucess");
                response.put("data", jsonArray);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (IOException e) {
                // If deserialization as array fails, treat it as a single JSON object
                try {
                    JsonNode jsonObject = objectMapper.readTree(concatenatedString);
                    response.put("status", "suceess");
                    response.put("Messag", "nodata is present");
                    response.put("data", jsonObject);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } catch (IOException ex) {
                    // If both attempts fail, return an error response
                    response.put("status", "fail");
                    response.put("Messag", "IOException");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (NoSuchMessageException e) {
             response.put("status","suceess");
                response.put("Messag","NoSuchMessageException");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
