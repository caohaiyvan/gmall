package com.example.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.gmall.bean.PaymentInfo;
import com.example.gmall.payment.mapper.PaymentMapper;
import com.example.gmall.service.PaymentService;
import com.example.gmall.utils.ActiveMQUtil;
import com.example.gmall.utils.SendMqUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chy
 * @date 2020/5/8 14:02
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    SendMqUtil sendMqUtil;
    @Autowired
    AlipayClient alipayClient;

    /**
     * 改变支付状态并发送消息队列，通知订单服务修改订单状态
     *
     * @param paymentInfo
     */
    @Override
    public void changePayment(PaymentInfo paymentInfo) {
        // 幂等性检查
        PaymentInfo dbPayment = paymentMapper.selectOne(paymentInfo);
        if(dbPayment != null && "已支付".equals(dbPayment.getPaymentStatus())){
            return;
        }
        Example e = new Example(PaymentInfo.class);
        e.createCriteria().andEqualTo("orderSn", paymentInfo.getOrderSn());
        Connection connection = null;
        Session session = null;

        try {
            // 发送消息，通知订单服务修改订单状态
            // 发送的消息
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderSn", paymentInfo.getOrderSn());

            Map map = sendMqUtil.sendMessage("PAYMENT_SUCCESS_QUEUE", mapMessage);
            connection = (Connection) map.get("connection");
            session = (Session) map.get("session");
            paymentMapper.updateByExampleSelective(paymentInfo, e);
        } catch (Exception ex) {
            // 消息回滚
            if (session != null) {
                try {
                    session.rollback();
                } catch (JMSException exc) {
                    exc.printStackTrace();
                }
            }
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException exc) {
                    exc.printStackTrace();
                }
            }


        }


    }

    /**
     * 保存支付信息
     *
     * @param paymentInfo
     */

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    /**
     * 发送延迟队列，检查支付状态
     *
     * @param orderSn
     */
    @Override
    public void sendDelayPaymentCheckQueue(String orderSn) {
        Connection connection = null;
        Session session = null;
        try {
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderSn", orderSn);
            // 设置延迟 param1:延迟方式 param2:延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 10000);
            Map map = sendMqUtil.sendMessage("PAYMENT_CHECK_QUENE", mapMessage);
            connection = (Connection) map.get("connection");
            session = (Session) map.get("session");
        } catch (Exception ex) {
            // 消息回滚
            if (session != null) {
                try {
                    session.rollback();
                } catch (JMSException exc) {
                    exc.printStackTrace();
                }
            }
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<String, String> checkAlipayPayment(String orderSn) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", orderSn);
        Map<String, String> resultMap = new HashMap<>();
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (response.isSuccess()) {
//                resultMap.put("out_trade_no", response.getOutTradeNo());
                resultMap.put("tradeStatus", response.getTradeStatus());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return resultMap;
    }
}
