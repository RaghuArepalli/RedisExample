﻿# RedisExample
This is a sample API to get the Redis connection for your spring boot application 
This appliaction will connect to the redis database and allow the post are update date into the redis 
there are two method which allow the user to create the a hashset(hset) and add data in into it 
and get method to fetch data from redis hashset(hset)
for this we use redistemplates this time will allow us to create varioues type of data into redis like streams,hashs,sets etc
http://localhost:8080/redishash/getredisdata?ucid=xxxxx
use this url for fetching data from redis

http://localhost:8080/redishash/postredisdata
this url is help you to post data in to the redis
