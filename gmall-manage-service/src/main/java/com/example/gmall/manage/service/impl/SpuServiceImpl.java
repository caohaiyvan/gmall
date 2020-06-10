package com.example.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.bean.PmsProductImage;
import com.example.gmall.bean.PmsProductInfo;
import com.example.gmall.bean.PmsProductSaleAttr;
import com.example.gmall.bean.PmsProductSaleAttrValue;
import com.example.gmall.manage.mapper.SpuImageMapper;
import com.example.gmall.manage.mapper.SpuSaleAttrMapper;
import com.example.gmall.manage.mapper.SpuSaleAttrValueMapper;
import com.example.gmall.manage.mapper.SupMapper;
import com.example.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    SupMapper supMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        return supMapper.select(pmsProductInfo);
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        supMapper.insertSelective(pmsProductInfo);
        for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductInfo.getSpuSaleAttrList()) {
            pmsProductSaleAttr.setProductId(pmsProductInfo.getId());
            spuSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                pmsProductSaleAttrValue.setProductId(pmsProductInfo.getId());
                spuSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }
        }
        for (PmsProductImage pmsProductImage : pmsProductInfo.getSpuImageList()) {
            pmsProductImage.setProductId(pmsProductInfo.getId());
            spuImageMapper.insertSelective(pmsProductImage);

        }
        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        // 获取销售属性列表
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> saleAttrList = spuSaleAttrMapper.select(pmsProductSaleAttr);
        // 获取销售属性值列表(lamda 遍历)
        saleAttrList.forEach(productSaleAttr -> {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            pmsProductSaleAttrValue.setProductId(spuId);
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = spuSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
        });
        // 增强for遍历
//        for (PmsProductSaleAttr productSaleAttr : saleAttrList) {
//            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
//            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
//            List<PmsProductSaleAttrValue> pmsProductSaleAttrValueList = spuSaleAttrValueMapper.select(pmsProductSaleAttrValue);
//            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValueList);
//        }

        return saleAttrList;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheck(String spuId, String skuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrList(spuId, skuId);
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        return spuImageMapper.select(pmsProductImage);
    }


}
