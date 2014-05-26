package com.mercury.util;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RabbitMQClient implements MQClient {

    @Resource(name = "amqpTemplate")
    private AmqpTemplate queue;

    @Resource(name = "amqpAdmin")
    private AmqpAdmin admin;

    @Override
    public void put(String queueName, Object object) {
        if (queueName == null || object == null) {
            throw new IllegalArgumentException("Parameters cannot be null. queueName=" + queueName + ", object=" + object);
        }
        queue.convertAndSend(queueName, object);
    }

    public Object retrieveFromQueue(String queueName) {
        if (queueName == null) {
            throw new IllegalArgumentException("Parameters cannot be null.");
        }
        return queue.receiveAndConvert(queueName);
    }

    void purgeQueue(String queueName) {
        admin.purgeQueue(queueName, false);
    }

    void createQueue(String queueName) {
        admin.declareQueue(new Queue(queueName));
    }

    void deleteQueue(String queueName) {
        admin.deleteQueue(queueName);
    }
}
