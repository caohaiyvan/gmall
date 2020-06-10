package com.example.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.example.gmall.annotations.LoginRequired;
import com.example.gmall.bean.OmsOrder;
import com.example.gmall.bean.PaymentInfo;
import com.example.gmall.payment.config.AlipayConfig;
import com.example.gmall.service.OrderService;
import com.example.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chy
 * @date 2020/4/23 10:18
 */

@Controller
@LoginRequired
public class PaymentController {
    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;


    @RequestMapping("index")
    public String index(HttpServletRequest request, RedirectAttributes attr, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        modelMap.put("orderSn", request.getParameter("orderSn"));
        modelMap.put("totalAmount", request.getParameter("totalAmount"));
        return "index";
    }

    /**
     * 支付宝支付
     *
     * @param orderSn
     * @return
     */

    @RequestMapping("alipay/submit")
    public String alipay(@RequestParam(value = "orderSn",required = false) String orderSn) {
        // 模拟数据
        orderSn = "110110110110110110110110";
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest(); //创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl("http://domain.com/CallBack/notify_url.jsp"); //在公共参数中设置回跳和通知地址
        OmsOrder order = orderService.getOrderInfoByOrderSn(orderSn);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setOrderSn(orderSn);
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setTotalAmount(order.getTotalAmount());
        paymentService.savePayment(paymentInfo);
        // 发送一个延迟队列检查支付状态
        paymentService.sendDelayPaymentCheckQueue(orderSn);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("orderSn",orderSn);
        paramMap.put("totalAmount", order.getTotalAmount());
        alipayRequest.setBizContent(JSON.toJSONString(paramMap)); //填充业务参数
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

    /**
     * 微信支付
     *
     * @param orderSn
     * @return
     */
    @RequestMapping("mx/submit")
    public String mxPay(@RequestParam("orderSn") String orderSn) {
        return null;
    }

    /**
     * 支付成功回调方法
     *
     * @return
     */
    @RequestMapping("alipay/callback/return")
    public String returnUrl() {
        // 更新用户支付状态
        PaymentInfo paymentInfo = new PaymentInfo();
        // 因支付服务无法使用，此处使用模拟数据测试消息队列
        paymentInfo.setOrderSn("110110110110110110110110"); // 模拟数据
        paymentInfo.setPaymentStatus("已支付");
        paymentService.changePayment(paymentInfo);
        return "finish";
    }
}
