package com.example.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.example.gmall.bean.PmsSearchParam;
import com.example.gmall.bean.PmsSearchSkuInfo;
import com.example.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chy
 * @date 2020/4/1 12:27
 */

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<PmsSearchSkuInfo>();
        String dslStr = this.buildDslStr(pmsSearchParam);
        Search search = new Search.Builder(dslStr).addIndex("gmall").addType("SkuInfo").build();
        SearchResult result = null;
        try {
            result = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result != null){
            result.getHits(PmsSearchSkuInfo.class).forEach(hit ->{
                PmsSearchSkuInfo source = hit.source;
                // 获取高亮信息，然后用高亮内容替换原来内容
                Map<String, List<String>> highlight = hit.highlight;
                if(highlight != null){
                    String skuName = highlight.get("skuName").get(0);
                    source.setSkuName(skuName);
                }

                pmsSearchSkuInfos.add(source);
            });
        }
        return pmsSearchSkuInfos;
    }

    /**
     * 构建查询字符串
     * @param pmsSearchParam
     * @return     */
    public String buildDslStr(PmsSearchParam pmsSearchParam){
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if(StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if(StringUtils.isNotBlank(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        if(valueIds != null){
            for (String valueId : valueIds) {
                if(StringUtils.isNotBlank(valueId)){
                    TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueId);
                    boolQueryBuilder.filter(termQueryBuilder);
                }
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlight(highlightBuilder);
        searchSourceBuilder.sort("id", SortOrder.ASC);
        String dslStr = searchSourceBuilder.toString();
        System.out.println(dslStr);
        return dslStr;
    }
}
