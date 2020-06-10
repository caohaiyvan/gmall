package com.example.gmall.service;

import com.example.gmall.bean.PmsBaseAttrInfo;
import com.example.gmall.bean.PmsBaseSaleAttr;
import com.example.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getBaseAttrInfo(List<PmsSearchSkuInfo> pmsSearchSkuInfoList);
}
