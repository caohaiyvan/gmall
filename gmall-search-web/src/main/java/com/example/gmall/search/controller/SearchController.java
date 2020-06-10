package com.example.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.bean.PmsBaseAttrInfo;
import com.example.gmall.bean.PmsSearchCrumb;
import com.example.gmall.bean.PmsSearchParam;
import com.example.gmall.bean.PmsSearchSkuInfo;
import com.example.gmall.service.AttrService;
import com.example.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author chy
 * @date 2020/4/1 12:27
 */

@Controller
@CrossOrigin
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService;

    @RequestMapping("index")
    // 加此注解是因为去认证中心时无需拦截，返回再访问index时需要拦截
    @LoginRequired(false)
    public String index() {
        return "index";
    }

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap, HttpServletRequest request) {
        List<PmsSearchCrumb> attrValueSelectedList = new ArrayList<>();
        // 查询参数串（地址URL里？后面的部分）
        String urlParam = request.getQueryString();
        // 从es中查询sku列表
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfoList);
        // 获取上面查询到的sku列表中sku对象的平台属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getBaseAttrInfo(pmsSearchSkuInfoList);


        // 对平台属性列表进行处理，选中一个平台属性就删除一组属性和属性值
        Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();

        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds != null) {
            while (iterator.hasNext()) {

                iterator.next().getAttrValueList().forEach(attrValue -> {
                    for (String valueId : valueIds) {
                        if (StringUtils.isNotBlank(valueId) && valueId.equals(attrValue.getId())) {
                            // 为实现面包屑，在删除之前获取选中的属性值对象
                            PmsSearchCrumb pmsSearchAttrValue = new PmsSearchCrumb();
                            pmsSearchAttrValue.setValueName(attrValue.getValueName());
                            // 实现点击面包屑，去掉选中的面包屑相应的valueId形成新的urlParam
                            String param = "";
                            if(StringUtils.isNotBlank(urlParam)){
                                int index = urlParam.lastIndexOf("&");
                                param = urlParam.substring(0,index);

                            }
                            pmsSearchAttrValue.setUrlParam(param);
                            attrValueSelectedList.add(pmsSearchAttrValue);

                            iterator.remove();
                        }
                    }
                });
            }

        }

        modelMap.put("attrList", pmsBaseAttrInfoList);

        modelMap.put("urlParam", urlParam);

        modelMap.put("keyword", pmsSearchParam.getKeyword());

        modelMap.put("attrValueSelectedList", attrValueSelectedList);

        return "list";
    }
}
