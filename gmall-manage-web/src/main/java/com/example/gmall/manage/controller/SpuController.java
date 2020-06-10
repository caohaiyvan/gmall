package com.example.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.bean.PmsProductImage;
import com.example.gmall.bean.PmsProductInfo;
import com.example.gmall.bean.PmsProductSaleAttr;
import com.example.gmall.manage.util.PmsUploadUtil;
import com.example.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        return spuService.spuList(catalog3Id);
    }

    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        //将文件上传到分布式文件系统
        String imgUrl = new PmsUploadUtil().uploadImage(multipartFile);
        //将存储路径返回给页面
        return FILE_SERVER_URL + imgUrl;
    }


    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        return spuService.saveSpuInfo(pmsProductInfo);
    }

    @GetMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        return spuService.spuSaleAttrList(spuId);
    }

    @GetMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId) {
        return spuService.spuImageList(spuId);
    }

}
