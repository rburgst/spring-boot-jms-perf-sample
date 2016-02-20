package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.PostConstruct;
import javax.jms.MapMessage;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sends JMS messages.
 *
 * @author Rainer Burgstaller
 */
public class Sender extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Sender.class);
    public static final int BUF_SIZE = 100;

    private final JmsTemplate jmsTemplate;
    private final String queueName;
    private final long waitBetweenMsgsInMs;
    volatile long exceptionCount;
    private final AtomicInteger group = new AtomicInteger(1);

    public Sender(JmsTemplate jmsTemplate, String queueName, long waitBetweenMsgsInMs) {
        this.jmsTemplate = jmsTemplate;
        this.queueName = queueName;
        this.waitBetweenMsgsInMs = waitBetweenMsgsInMs;
    }

    @PostConstruct
    public void postConstruct() {
        this.start();
    }

    @Override
    public void run() {

        // Send a message
        MessageCreator messageCreator = session -> {
            final MapMessage mapMessage = session.createMapMessage();
            mapMessage.setIntProperty("CLIENT_ID", group.get());
            mapMessage.setString("key", "hallohallohallohallohallohallohallohallohallohallohallohallohallohallohallohallohallohallo");
            return mapMessage;
        };

        long sendCount = 100000;
        long start = System.currentTimeMillis();
        float messagesPerSecond = 0;
        float messagesPerSecondCurBuffer = 0;
        long curBufferStart = start;
        int messagesPerGroup = 10;
        int groupToGo = messagesPerGroup;

        for (int i = 0; i < sendCount; i++) {
            if (groupToGo-- == 0) {
                groupToGo = messagesPerGroup;
                // toggle group
                if (group.incrementAndGet() > 2) {
                    group.set(1);
                }
            }

            if (i % BUF_SIZE == 0) {
                messagesPerSecond = Application.calculateMessagesPerSeconds(start, i);
                messagesPerSecondCurBuffer = Application.calculateMessagesPerSeconds(curBufferStart, BUF_SIZE);
                curBufferStart = System.currentTimeMillis();
            }
            sleepIfNecessary();
            try {
                jmsTemplate.send(queueName, messageCreator);
                logger.debug("Sending a new message {}/{} group {}, {} msg/s (cur: {} msg/s), exception: {}", i, sendCount,
                        group.get(), messagesPerSecond, messagesPerSecondCurBuffer, exceptionCount);
            } catch (Exception e) {
                logger.error("Error sending message {}", i, e);
                exceptionCount++;
            }
        }

    }

    private void sleepIfNecessary() {
        if (waitBetweenMsgsInMs > 0) {
            try {
                sleep(waitBetweenMsgsInMs);
            } catch (InterruptedException e) {
                logger.error("error sleeping", e);
            }
        }
    }
}
