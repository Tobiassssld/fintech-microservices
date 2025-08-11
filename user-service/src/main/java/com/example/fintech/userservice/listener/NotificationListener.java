package com.example.fintech.userservice.listener;

import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotificationEvent(NotificationEvent event) {
        logger.info("=== processing notification event ===");
        logger.info("User ID: {}", event.getUserId());
        logger.info("Title, {}", event.getTitle());
        logger.info("Message, {}", event.getMessage());
        logger.info("Type, {}", event.getType());
        logger.info("Timestamp:{}", event.getTimestamp());

        switch (event.getType()) {
            case "EMAIL":
                sendEmail(event);
                break;
            case "SMS":
                sendSMS(event);
                break;
            case "PUSH":
                sendPushNotification(event);
                break;
            default:
                logger.warn("unknown notification type: {}", event.getType());
        }
        logger.info("notification processsed successfully.");
    }

    private void sendEmail(NotificationEvent event){
        logger.info("Sending SMS to user {}: {}", event.getUserId(), event.getTitle());
    }

    private void sendSMS(NotificationEvent event){
        logger.info("Sending SMS to user {}: {}", event.getUserId(), event.getTitle());
    }

    private void sendPushNotification(NotificationEvent event){
        logger.info("Sending SMS to user {}: {}", event.getUserId(), event.getTitle());
    }
}
