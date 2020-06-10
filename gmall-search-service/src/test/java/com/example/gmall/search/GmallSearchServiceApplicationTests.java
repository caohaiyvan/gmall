package com.example.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.bean.PmsSearchSkuInfo;
import com.example.gmall.bean.PmsSkuInfo;
import com.example.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Reference
    SkuService skuService;
    @Autowired
    JestClient jestClient;
    @Test
    public void cextLoads() throws IOException {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // filter
        // term
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id","61");
        boolQueryBuilder.filter(termQueryBuilder);
        // must
        //match
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName","小米");
        boolQueryBuilder.must(matchQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        searchSourceBuilder.highlight(null);
        String dslStr = searchSourceBuilder.toString();
        System.out.println(dslStr);
        Search search = new Search.Builder(dslStr).addIndex("gmall").addType("SkuInfo").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        hits.forEach(hit ->{
            PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
            System.out.println(pmsSearchSkuInfo.toString());
//            pmsSearchSkuInfo.getSkuAttrValueList().forEach(skuAttrValue ->{
//                System.out.println(skuAttrValue.toString());
//            });

        });
        System.out.println(pmsSearchSkuInfos.size());


    }

    /**
     * 原生dsl语句查询
     * @throws IOException
     */

    public void dslSearch() throws IOException{
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder("{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "         \n" +
                "          {\"term\":{\"catalog3Id\":\"61\"}}\n" +
                "        ],\n" +
                "        \"must\": \n" +
                "            { \"match\": { \"skuName\": \"华为\" }  }\n" +
                "    }\n" +
                "  }\n" +
                "  , \n" +
                "  \n" +
                "  \"size\": 1, \n" +
                "  \n" +
                "  \"aggs\": {\n" +
                "    \"groupby_attr\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"skuAttrValueList.valueId\"  \n" +
                "      }\n" +
                "    } \n" +
                "    \n" +
                "  }\n" +
                "}").addIndex("gmall").addType("SkuInfo").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        hits.forEach(hit ->{
            PmsSearchSkuInfo pmsSearchSkuInfo = hit.source;
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
            System.out.println(pmsSearchSkuInfo.toString());
            pmsSearchSkuInfo.getSkuAttrValueList().forEach(skuAttrValue ->{
                System.out.println(skuAttrValue.toString());
            });
        });
        System.out.println(pmsSearchSkuInfos.size());
    }

    /**
     * 初始化数据
     */
    @Test
    public void put(){
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        List<PmsSkuInfo> pmsSkuInfoList = new ArrayList<>();
        pmsSkuInfoList = skuService.geAlltSku();

        pmsSkuInfoList.forEach(pmsSkuInfo -> {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            try {
                BeanUtils.copyProperties(pmsSearchSkuInfo,pmsSkuInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        });
        pmsSearchSkuInfoList.forEach(searchSkuInfo ->{
            Index index = new Index.Builder(searchSkuInfo).index("gmall").type("SkuInfo").id(searchSkuInfo.getId()).build();

            try {
                jestClient.execute(index);
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }

}
