package com.example.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.bean.UmsMember;
import com.example.gmall.bean.UmsMemberReceiveAddress;
import com.example.gmall.service.UserService;
import com.example.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.example.gmall.user.mapper.UserMapper;
import com.example.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper receiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
        return userMapper.selectAll();
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId, String id) {
        UmsMemberReceiveAddress receiveAddress = new UmsMemberReceiveAddress();
        receiveAddress.setId(id);
        receiveAddress.setMemberId(memberId);

        return receiveAddressMapper.select(receiveAddress);
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        UmsMember user = null;
        Jedis jedis = null;
        // 先查询缓存
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String userInfo = jedis.get("user:" + umsMember.getUsername() + ":info");
                if (StringUtils.isNotBlank(userInfo)) {
                    user = JSON.parseObject(userInfo, UmsMember.class);
                    String password = umsMember.getPassword();
                    if (StringUtils.isNotBlank(password) && password.equals(user.getPassword())) {
                        return user;
                    } else {
                        // 密码不正确
                        return null;
                    }
                }
                // 缓存中用户不存在，查询数据库
                user = userMapper.selectOne(umsMember);
                if (user != null) {
                    jedis.setex("user:" + umsMember.getUsername() + ":info", 60 * 60 * 24, JSON.toJSONString(user));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return user;
    }

    @Override
    public void addTokenUser(String token, String userJson) {
        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            jedis.setex(token, 60 * 60 * 24, userJson);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    @Override
    public UmsMember userVerify(String token) {
        Jedis jedis = null;
        UmsMember umsMember = null;

        try {
            jedis = redisUtil.getJedis();
            String userJson = jedis.get(token);
            if (StringUtils.isNotBlank(userJson))
                umsMember = JSON.parseObject(userJson, UmsMember.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return umsMember;
    }

    @Override
    public UmsMember checkLoginRecord(UmsMember umsMember) {
        return userMapper.selectOne(umsMember);
    }

    @Override
    public void addUserToDb(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
    }

}
