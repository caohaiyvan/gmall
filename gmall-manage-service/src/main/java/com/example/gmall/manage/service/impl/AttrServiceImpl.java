package com.example.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.bean.PmsBaseAttrInfo;
import com.example.gmall.bean.PmsBaseAttrValue;
import com.example.gmall.bean.PmsBaseSaleAttr;
import com.example.gmall.bean.PmsSearchSkuInfo;
import com.example.gmall.manage.mapper.AttrInfoMapper;
import com.example.gmall.manage.mapper.AttrValueMapper;
import com.example.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.example.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    AttrInfoMapper attrInfoMapper;
    @Autowired
    AttrValueMapper attrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrInfoMapper.select(pmsBaseAttrInfo);
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfoList) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValueList = attrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValueList);

        }
        return pmsBaseAttrInfoList;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        attrInfoMapper.insertSelective(pmsBaseAttrInfo);
        pmsBaseAttrInfo.getAttrValueList();
        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            attrValueMapper.insertSelective(pmsBaseAttrValue);
        }

        return "success";
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getBaseAttrInfo(List<PmsSearchSkuInfo> pmsSearchSkuInfoList) {
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = new ArrayList<>();
        Set<String> valueIds = new HashSet();
        Set<String> attrIds = new HashSet();
        pmsSearchSkuInfoList.forEach(searchInfo ->{
            searchInfo.getSkuAttrValueList().forEach(attrValue ->{
                attrIds.add(attrValue.getAttrId());
                valueIds.add(attrValue.getValueId());
            });
        });
        String attrIdStr = StringUtils.join(attrIds, ",");
        String valueIdStr = StringUtils.join(valueIds, ",");
        pmsBaseAttrInfos = attrInfoMapper.selectSkuBaseAttrInfo(attrIdStr,valueIdStr);
        return pmsBaseAttrInfos;
    }
}
