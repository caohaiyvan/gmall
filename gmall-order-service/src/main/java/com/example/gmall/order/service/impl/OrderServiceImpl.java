package com.example.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.bean.OmsOrder;
import com.example.gmall.order.mapper.OrderItemMapper;
import com.example.gmall.order.mapper.OrderMapper;
import com.example.gmall.service.OrderService;
import com.example.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.Collections;
import java.util.UUID;

/**
 * @author chy
 * @date 2020/4/21 16:35
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;

    /**
     * 生成交易码
     *
     * @param memberId
     * @return
     */
    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            jedis = redisUtil.getJedis();
            System.out.println("交易码： " + tradeCode);
            jedis.setex("user:" + memberId + ":tradeCode", 60 * 15, tradeCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return tradeCode;
    }

    /**
     * 校验交易码
     *
     * @param memberId
     * @param tradeCode
     * @return
     */
    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            // 使用lua脚本防止多并发提交（替换下面注释代码）
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Long eval = (Long) jedis.eval(script, Collections.singletonList("user:" + memberId + ":tradeCode"),
                    Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                return true;
            }
            /*String cacheTradeCode = jedis.get("user:" + memberId + ":tradeCode");
            if(StringUtils.isNotBlank(cacheTradeCode) && cacheTradeCode.equals(tradeCode)){
                // 此处直接删除交易码，比执行提交订单代码后再删除交易码效果更好
                jedis.del("user:" + memberId + ":tradeCode");
                return true;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public void saveOrder(OmsOrder order) {
        orderMapper.insertSelective(order);
        order.getOmsOrderItems().forEach(omsOrderItem -> {
            omsOrderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(omsOrderItem);
        });
    }

    @Override
    public OmsOrder getOrderInfoByOrderSn(String orderSn) {
        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        return orderMapper.selectOne(order);
    }

    @Override
    public void changeOrder(OmsOrder order) {
        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn", order.getOrderSn());
        orderMapper.updateByExampleSelective(order, e);
    }

    /**
     * 删除交易码
     * @param memberId
     */
//    @Override
//    public void delTradeCode(String memberId) {
//        Jedis jedis = null;
//        try {
//            jedis = redisUtil.getJedis();
//            jedis.del("user:" + memberId + ":tradeCode");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            jedis.close();
//        }
//    }
}
