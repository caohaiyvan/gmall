package com.example.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.bean.PmsProductSaleAttr;
import com.example.gmall.bean.PmsSkuInfo;
import com.example.gmall.service.SkuService;
import com.example.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("index")
    public String index(ModelMap modelMap) {
        modelMap.put("hello", "hello thymeleaf");
        return "index";
    }

    @GetMapping("{skuId}.html")
    public String getSkuInfo(@PathVariable("skuId") String skuId, ModelMap modelMap) {
        // skuSaleAttrHashJsonStr

        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.spuSaleAttrListCheck(skuInfo.getProductId(), skuId);
        modelMap.put("skuInfo", skuInfo);
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrList);

        return "item";
    }
}
