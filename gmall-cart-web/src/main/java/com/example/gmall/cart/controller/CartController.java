package com.example.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.bean.OmsCartItem;
import com.example.gmall.bean.PmsSkuInfo;
import com.example.gmall.constant.SysConstant;
import com.example.gmall.service.CartService;
import com.example.gmall.service.SkuService;
import com.example.gmall.util.CookieUtil;
import com.example.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chy
 * @date 2020/4/10 10:27
 */

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;


    @RequestMapping("addToCart")
    @LoginRequired(value = false)
    public String addToCart(@RequestParam(value = "skuId", required = false) String skuId, @RequestParam("num") String num, ModelMap modelMap,
                            HttpServletRequest request, HttpServletResponse response) {
        BigDecimal quantity = new BigDecimal(num);
        // 查询sku信息
        PmsSkuInfo skuInfo = skuService.getSkuInfo(skuId);
        modelMap.put("skuInfo", skuInfo);
        this.addCartToDbAndCookie(request, response, skuInfo, quantity, skuId);

        return "redirect:/success.html";
    }


    public void addCartToDbAndCookie(HttpServletRequest request, HttpServletResponse response, PmsSkuInfo skuInfo, BigDecimal quantity, String skuId) {
        // 封装成购物项对象
        String memberId = (String) request.getAttribute("memberId");
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getSpuId());
        omsCartItem.setProductSkuId(skuInfo.getId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setIsChecked("1");
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setQuantity(quantity);
        omsCartItem.setTotalPrice(skuInfo.getPrice().multiply(quantity));
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String access_token = CookieUtil.getCookieValue(request, "access_token", true);
        Map<String, Object> userMap = null;
        if (StringUtils.isNotBlank(access_token))
            userMap = JwtUtil.decode(access_token, SysConstant.ENCRYKEY, SysConstant.SALT);
        if (StringUtils.isBlank(memberId)) { // 用户未登录,购物项信息存到cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isBlank(cartListCookie)) { // 未添加过商品

                omsCartItems.add(omsCartItem);
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
            } else {
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean b = is_exist_cartItemCookie(omsCartItems, omsCartItem);
                if (!b) {
                    omsCartItems.add(omsCartItem);
                }
                CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);

            }

        } else { // 用户已登录,购物项信息存到数据库
            omsCartItem.setMemberNickname((String) userMap.get("nickName"));
            // 合并购物车
            List<OmsCartItem> cookieCartItems = null;
            String cartJson = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartJson)) {
                cookieCartItems = JSON.parseArray(cartJson, OmsCartItem.class);
            }
            if (cookieCartItems != null) {
                cookieCartItems.forEach(cartItem -> {
                    this.addCartToDb(omsCartItem.getMemberId(), cartItem.getProductSkuId(), cartItem.getQuantity(), cartItem);
                });
            }
            // 登录后添加到购物车
            this.addCartToDb(memberId, skuId, quantity, omsCartItem);

        }

    }

    public void addCartToDb(String memberId, String skuId, BigDecimal quantity, OmsCartItem omsCartItem) {
        OmsCartItem dbCartItem = cartService.is_exist_cartItemDb(memberId, skuId);
        if (dbCartItem != null) {
            dbCartItem.setQuantity(dbCartItem.getQuantity().add(quantity));
            cartService.editCartItem(dbCartItem);
            // 更新redis缓存
            cartService.flushCache(dbCartItem);
        } else {
            cartService.addCartItem(omsCartItem);
            // 新增redis缓存
            cartService.flushCache(omsCartItem);
        }
    }

    private static boolean is_exist_cartItemCookie(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        AtomicBoolean b = new AtomicBoolean(false);
        omsCartItems.forEach(cartItem -> {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                b.set(true);
            }
        });
        return b.get();
    }

    @RequestMapping("cartList")
    @LoginRequired(false)
    public String cartList(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        List<OmsCartItem> cartList = new ArrayList<>();
        // 获取用户ID
        String memberId = (String) request.getAttribute("memberId");
        // 用户已登录，缓存中查询
        if (StringUtils.isNotBlank(memberId)) {
            cartList = cartService.cartList(memberId);

        } else { // 用户未登录，cookie中获取
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                cartList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }


        }

        modelMap.put("cartList", cartList);
        modelMap.put("userId", memberId);

        return "cartList";

    }

}
