package com.example.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.bean.OmsCartItem;
import com.example.gmall.cart.mapper.CartMapper;
import com.example.gmall.service.CartService;
import com.example.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chy
 * @date 2020/4/10 12:12
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartMapper cartMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem is_exist_cartItemDb(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        return cartMapper.selectOne(omsCartItem);
    }

    @Override
    public void editCartItem(OmsCartItem dbCartItem) {
        cartMapper.updateByPrimaryKeySelective(dbCartItem);

    }

    @Override
    public void flushCache(OmsCartItem dbCartItem) {
        Jedis jedis = redisUtil.getJedis();
        jedis.hset("user:" + dbCartItem.getMemberId() + ":cart",dbCartItem.getProductSkuId(), JSON.toJSONString(dbCartItem) );

    }

    @Override
    public void addCartItem(OmsCartItem omsCartItem) {
        cartMapper.insertSelective(omsCartItem);

    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {

        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            hvals.forEach(val ->{
                OmsCartItem omsCartItem = JSON.parseObject(val, OmsCartItem.class);
                omsCartItemList.add(omsCartItem);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return omsCartItemList;
    }

    @Override
    public void removeCartByMemberIdAndIschecked(String memberId) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setProductSkuId(memberId);
        cartItem.setIsChecked("1");
        cartMapper.delete(cartItem);
    }
}
