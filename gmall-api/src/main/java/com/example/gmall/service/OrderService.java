package com.example.gmall.service;

import com.example.gmall.bean.OmsOrder;

/**
 * @author chy
 * @date 2020/4/21 16:31
 */

public interface OrderService {
    String genTradeCode(String memberId);

    boolean checkTradeCode(String memberId, String tradeCode);

    void saveOrder(OmsOrder order);

    OmsOrder getOrderInfoByOrderSn(String orderSn);

    void changeOrder(OmsOrder order);
//    void delTradeCode(String memberId);
}
