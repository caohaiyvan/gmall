package com.example.gmall.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chy
 * @date 2020/5/9 18:54
 */
@Component
public class SendMqUtil {
    @Autowired
    ActiveMQUtil activeMQUtil;

    public Map sendMessage(String mqName, MapMessage mapMessage) {
        Map<String, Object> map = new HashMap<>();
        Connection connection = null;
        Session session = null;
        try {
            // 发送消息，通知订单服务修改订单状态
            connection = activeMQUtil.getConnectionFactory().createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            // 第二个值为开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 队列模式
            Queue testqueue = session.createQueue(mqName);
            MessageProducer producer = session.createProducer(testqueue);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        map.put("connection", connection);
        map.put("session", session);
        return map;
    }
}
