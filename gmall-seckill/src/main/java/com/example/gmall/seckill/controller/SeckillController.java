package com.example.gmall.seckill.controller;

import com.example.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * @author chy
 * @date 2020/5/11 13:54
 */
@Controller
public class SeckillController {
    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("kill")
    @ResponseBody
    public String kill() {
        Jedis jedis = redisUtil.getJedis();
        int stock = Integer.parseInt(jedis.get("106"));
        jedis.watch("106");
        if (stock > 0) {
            Transaction multi = jedis.multi();
            multi.incrBy("106", -1);
            List<Object> exec = multi.exec();
            if (exec != null && exec.size() > 0){
                System.out.println("秒杀成功");
                // 消息队列发送订单消息
            }else{
                System.out.println("秒杀失败");

            }
        }
        return "1";
    }
}
