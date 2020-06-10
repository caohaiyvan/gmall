package com.example.gmall.service;

import com.example.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author chy
 * @date 2020/5/8 14:01
 */

public interface PaymentService {
    void changePayment(PaymentInfo paymentInfo);

    void savePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentCheckQueue(String orderSn);

    Map<String, String> checkAlipayPayment(String orderSn);
}
