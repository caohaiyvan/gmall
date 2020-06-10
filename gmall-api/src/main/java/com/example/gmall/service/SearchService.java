package com.example.gmall.service;

import com.example.gmall.bean.PmsSearchParam;
import com.example.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * @author chy
 * @date 2020/4/1 12:27
 */

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
