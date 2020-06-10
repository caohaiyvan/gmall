package com.example.gmall.order.mq;

import com.example.gmall.bean.OmsOrder;
import com.example.gmall.service.OrderService;
import com.example.gmall.utils.ActiveMQUtil;
import com.example.gmall.utils.SendMqUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author chy
 * @date 2020/5/9 17:58
 */
@Component
public class OrderServiceMqListener {
    @Autowired
    OrderService orderService;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    SendMqUtil sendMqUtil;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        if (mapMessage.itemExists("orderSn")) {
            // 修改订单状态
            OmsOrder order = new OmsOrder();
            order.setOrderSn(mapMessage.getString("orderSn"));
            order.setStatus("1");
            orderService.changeOrder(order);
            // @TODO 向库存系统发送消息，通知其锁定商品
//            MapMessage message = new ActiveMQMapMessage();
//            message.setString("");
//            sendMqUtil.sendMessage("ORDER_SUCCESS_QUEUE",message);

        }


    }
}
