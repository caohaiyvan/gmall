package com.example.gmall.bean;

import lombok.Data;

import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
@Data
public class PmsSearchSkuInfo implements Serializable {
    @Id
    private String id;
    private BigDecimal price;
    private String skuName;
    private String skuDesc;
    private String catalog3Id;
    private String skuDefaultImg;
    private List<PmsSkuAttrValue> skuAttrValueList;


}