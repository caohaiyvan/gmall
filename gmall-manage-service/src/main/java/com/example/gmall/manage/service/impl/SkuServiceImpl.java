package com.example.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.example.gmall.bean.PmsSkuAttrValue;
import com.example.gmall.bean.PmsSkuImage;
import com.example.gmall.bean.PmsSkuInfo;
import com.example.gmall.bean.PmsSkuSaleAttrValue;
import com.example.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.example.gmall.manage.mapper.SkuAttrValueMapper;
import com.example.gmall.manage.mapper.SkuImageMapper;
import com.example.gmall.manage.mapper.SkuMapper;
import com.example.gmall.service.SkuService;
import com.example.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper attrValueMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public PmsSkuInfo getSkuInfo(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        // 查询sku
        PmsSkuInfo skuInfo = skuMapper.selectOne(pmsSkuInfo);
        // 查询图片列表
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImageList = skuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImageList);
        // 查询销售属性值列表
        PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
        pmsSkuSaleAttrValue.setSkuId(skuId);
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValueList = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(pmsSkuSaleAttrValueList);
        return skuInfo;
    }


    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = null;
        Jedis jedis = null;
        try {
            // 链接缓存
            jedis = redisUtil.getJedis();
            // 查询缓存
            String skuKey = "sku:" + skuId + ":info";
            // 数据库中存储value的策略为json字符串
            String skuJson = jedis.get(skuKey);
            if (StringUtils.isNotBlank(skuJson)) {
                // 将json字符串转换为 PmsSkuInfo 对象
                pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
                System.out.println(Thread.currentThread().getName() + " 从缓存中获得了数据");


            } else {
                // 若缓存中没有数据，则查询数据库,并把查询结果存入redis
                // 查询数据数据库前为减轻访问压力，需加上分布式锁，这里使用的是redis自带的锁
                // 为key赋一个随机的值，用以后面判断锁是不是同一把锁
                String token = UUID.randomUUID().toString();
                String ok = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10);
                if (StringUtils.isNotBlank(ok) && ok.equals("OK")) {
                    pmsSkuInfo = getSkuInfo(skuId);
                    if (pmsSkuInfo != null) {
                        // 此处睡眠5s，为了能显示自旋的效果，因为可能第一个进来的线程很快就执行完毕把数据放入缓存中了
                        Thread.sleep(5000);
                        jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                        System.out.println(Thread.currentThread().getName() + " 创建锁成功");
                        System.out.println(Thread.currentThread().getName() + " 从数据库中获得了数据，并把数据放入缓存中了");

                    } else {
                        // 若数据库中也没有数据，则将null或空字符串放到redis中，防止缓存穿透
                        // 设置过期时间，则该请求三分钟之内不会再访问mysql
                        jedis.setex(skuKey, 60 * 3, JSON.toJSONString(""));
                        System.out.println(Thread.currentThread().getName() + " 查询数据库为空值");
                    }
                    // 获取锁的value，判断是否是当前线程的锁，若是则删除锁，但是此方式有一个漏洞，
                    // 就是若判断锁时锁存在，在进行删除时，因为判断删除和存在时间差，刚好锁失效就会删除其他线程的锁
                    //因此不使用此方式删除锁（此代码注掉），而选用Lua脚本实现
//                    String tokenLock = jedis.get("sku:" + skuId + ":lock");
//                    if(StringUtils.isNotBlank(tokenLock) && tokenLock.equals(token)){
//                        jedis.del("sku:" + skuId + ":lock");
//                        System.out.println(Thread.currentThread().getName() + " 删除锁成功");
//                    }
                    // lua脚本实现如下
//                    String script = "if redis.call";


                } else {
                    // 锁设置失败，自旋（该线程休息几秒后，重新访问该方法）
                    Thread.sleep(3000);
                    System.out.println(Thread.currentThread().getName() + " 自旋重新访问该方法");
                    return getSkuById(skuId);

                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();

            }

        }

        return pmsSkuInfo;
    }

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        skuMapper.insertSelective(pmsSkuInfo);
        pmsSkuInfo.getSkuImageList().forEach(image -> {
            image.setSkuId(pmsSkuInfo.getId());
            skuImageMapper.insertSelective(image);
        });
        pmsSkuInfo.getSkuAttrValueList().forEach(attrValue -> {
            attrValue.setSkuId(pmsSkuInfo.getId());
            attrValueMapper.insertSelective(attrValue);

        });
        pmsSkuInfo.getSkuSaleAttrValueList().forEach(saleAttrValue -> {
            saleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(saleAttrValue);
        });

    }

    @Override
    public List<PmsSkuInfo> geAlltSku() {
        List<PmsSkuInfo> skuInfos = skuMapper.selectAll();
        skuInfos.forEach(pmsSkuInfo -> {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> pmsSkuAttrValues = attrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        });
        return skuInfos;
    }
}
