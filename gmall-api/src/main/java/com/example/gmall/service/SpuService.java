package com.example.gmall.service;

import com.example.gmall.bean.PmsProductImage;
import com.example.gmall.bean.PmsProductInfo;
import com.example.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);

    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheck(String spuId, String skuId);


    List<PmsProductImage> spuImageList(String spuId);
}
