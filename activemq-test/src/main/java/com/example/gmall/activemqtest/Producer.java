package com.example.gmall.activemqtest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * @author chy
 * @date 2020/5/7 12:52
 */

public class Producer {
    public static void main(String[] args) {


        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://192.168.200.128:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            // 第二个值为开启事务
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 队列模式
//            Queue testqueue = session.createQueue("TEST1");
            // 话题模式
            Topic testTopic = session.createTopic("TEST2");

            MessageProducer producer = session.createProducer(testTopic);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("今天天气真好！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
