package com.example.gmall.service;

import com.example.gmall.bean.PmsSkuInfo;

import java.util.List;

public interface SkuService {
    PmsSkuInfo getSkuInfo(String skuId);

    PmsSkuInfo getSkuById(String skuId);

    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    List<PmsSkuInfo> geAlltSku();
}
