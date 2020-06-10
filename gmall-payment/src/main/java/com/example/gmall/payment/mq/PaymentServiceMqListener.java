package com.example.gmall.payment.mq;

import com.example.gmall.bean.PaymentInfo;
import com.example.gmall.service.PaymentService;
import com.example.gmall.utils.ActiveMQUtil;
import com.example.gmall.utils.SendMqUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

/**
 * @author chy
 * @date 2020/5/9 17:58
 */
@Component
public class PaymentServiceMqListener {
    @Autowired
    PaymentService paymentService;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    SendMqUtil sendMqUtil;

    @JmsListener(destination = "PAYMENT_CHECK_QUENE", containerFactory = "jmsQueueListener")
    public void consumeCheckPaymentResult(MapMessage mapMessage) throws JMSException {
        if (mapMessage.itemExists("orderSn")) {
            String orderSn = mapMessage.getString("orderSn");
            // 调用支付宝检查接口
            Map<String, String> map = paymentService.checkAlipayPayment(orderSn);
            // 支付失败，继续发送延迟消息队列
            if(map.isEmpty()){
                paymentService.sendDelayPaymentCheckQueue(orderSn);
                return;
            }
            // 支付成功，改变支付状态
            String tradeStatus = map.get("tradeStatus");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(orderSn); // 模拟数据
            paymentInfo.setPaymentStatus("已支付");
            if("TRADE_SUCCESS".equals(tradeStatus)){
                paymentService.changePayment(paymentInfo);
            }



        }


    }
}
