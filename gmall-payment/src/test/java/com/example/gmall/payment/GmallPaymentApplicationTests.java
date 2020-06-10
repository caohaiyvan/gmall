package com.example.gmall.payment;

import com.example.gmall.utils.ActiveMQUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.jms.ConnectionFactory;

@SpringBootTest
class GmallPaymentApplicationTests {
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Test
    void contextLoads() {
        ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
        System.out.println(connectionFactory);

    }



}
