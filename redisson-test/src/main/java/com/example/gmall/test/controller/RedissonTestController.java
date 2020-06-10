package com.example.gmall.test.controller;

import com.example.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@RestController
public class RedissonTestController {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;
    @RequestMapping("lockTest")
    public String lockTest(){
        Jedis jedis = redisUtil.getJedis();// redis链接
        RLock lock = redissonClient.getLock("redis-lock");//分布锁
        //加锁
        lock.lock();
        try {
            String v = jedis.get("k");//获取value
            System.err.print("==>"+v);//打印value
            if(StringUtils.isBlank(v)){
                v = "1";
            }
            int inum = Integer.parseInt(v);//获得value的值
            jedis.set("k", inum+1+"");//value增加1
            jedis.close();
        } finally {
            lock.unlock();
        }
        return "success";
    }

    }
