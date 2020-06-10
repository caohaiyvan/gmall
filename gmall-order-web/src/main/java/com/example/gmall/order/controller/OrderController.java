package com.example.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.bean.*;
import com.example.gmall.service.CartService;
import com.example.gmall.service.OrderService;
import com.example.gmall.service.SkuService;
import com.example.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author chy
 * @date 2020/4/20 10:11
 */

@Controller
public class OrderController {
    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    @RequestMapping("toTrade")
    @LoginRequired
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {
        // 获取用户ID
        String memberId = (String) request.getAttribute("memberId");
        // 根据id查询购物车列表，将购物车列表转化为订单项列表
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> orderDetailList = new ArrayList<>();
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem cartItem : omsCartItems) {
            // @TODO cartItem选中的才进行封装,因我并未实现动态改变数据库中购物项选中状态的功能，因此此处默认是全部选中
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setProductPic(cartItem.getProductPic());
            orderItem.setProductPrice(cartItem.getPrice());
            orderItem.setProductQuantity(cartItem.getQuantity());
            orderItem.setProductSkuId(cartItem.getProductSkuId());
            // 计算总价
            totalAmount = totalAmount.add(cartItem.getTotalPrice());
            orderDetailList.add(orderItem);
        }
        // 生成提交订单流水号
        String tradeCode = orderService.genTradeCode(memberId);

        // 根据id查询收货地址列表
        List<UmsMemberReceiveAddress> receiveAddressList = userService.getReceiveAddressByMemberId(memberId, null);
        modelMap.put("orderDetailList", orderDetailList);
        modelMap.put("userAddressList", receiveAddressList);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }


    @RequestMapping("submitOrder")
    @LoginRequired
    public String submitOrder(String receiveAddressId, String totalAmount, String tradeCode, HttpServletRequest request, RedirectAttributes attr) {
        // 获取用户ID
        String memberId = (String) request.getAttribute("memberId");
        // 校验交易码
        boolean hasTradeCode = orderService.checkTradeCode(memberId, tradeCode);
        if (!hasTradeCode) {
            return null;
        }

        OmsOrder order = this.genOrder(memberId, receiveAddressId, totalAmount);
        // 将订单和订单项（来源于数据库中的购物车）存入数据库
        orderService.saveOrder(order);
        // 删除购物车中的商品
        cartService.removeCartByMemberIdAndIschecked(memberId);

        attr.addAttribute("orderSn",order.getOrderSn());
        attr.addAttribute("totalAmount",order.getTotalAmount());
        // 重定向到支付系统
        return "redirect://payment.mall.com:8087/index";
    }

    private OmsOrder genOrder(String memberId, String receiveAddressId, String totalAmount) {
        OmsOrder order = new OmsOrder();
        List<OmsOrderItem> omsOrderItems = order.getOmsOrderItems();
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        String orderSn = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + UUID.randomUUID().toString().replaceAll("-", "");
        omsCartItems.forEach(omsCartItem -> {
            // 校验价格
            if ("1".equals(omsCartItem.getIsChecked())) {
                String skuId = omsCartItem.getProductSkuId();
                // @TODO 校验库存,远程调用库存系统
                PmsSkuInfo sku = skuService.getSkuById(skuId);
                if (sku.getPrice().compareTo(omsCartItem.getPrice()) == 0) {
                    OmsOrderItem orderItem = new OmsOrderItem();
                    try {
                        orderItem.setProductSkuId(omsCartItem.getProductSkuId());
                        orderItem.setProductQuantity(omsCartItem.getQuantity());
                        orderItem.setProductPic(omsCartItem.getProductPic());
                        orderItem.setProductName(omsCartItem.getProductName());
                        orderItem.setProductPrice(omsCartItem.getPrice());
                        orderItem.setOrderSn(orderSn);
                        omsOrderItems.add(orderItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    // 价格改变了
                }
            }
        });
        List<UmsMemberReceiveAddress> receiveAddressByMemberId = userService.getReceiveAddressByMemberId(memberId, receiveAddressId);
        if (receiveAddressByMemberId != null) {
            UmsMemberReceiveAddress receiveAddress = receiveAddressByMemberId.get(0);
            order.setReceiverName(receiveAddress.getName());
            order.setReceiverDetailAddress(receiveAddress.getProvince() + receiveAddress.getCity() + receiveAddress.getRegion());
            order.setReceiverPhone(receiveAddress.getPhoneNumber());
        }
        order.setCreateTime(new Date());
        order.setDeleteStatus(0);
        order.setMemberId(memberId);
        order.setOrderSn(orderSn);
        order.setTotalAmount(new BigDecimal(totalAmount));
        order.setPayAmount(new BigDecimal(totalAmount));


        return order;
    }
}
