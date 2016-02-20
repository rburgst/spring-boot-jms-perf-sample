package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.MapMessage;

/**
 * Receives JMS messages from the configured queue using the configured selector.
 */
@Component
@Profile("receiver")
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);
    private static final int BUF_SIZE = 100;

    long messageReceived = 0;
    long timestampOfFirstMessage = 0;
    float messagesPerSecond = 0;
    float messagesPerSecondLastBuffer = 0;

    private final String selector;
    private long timestampLastBuffer;

    @Autowired
    public Receiver(@Value("${jms.selector}") String selector) {
        this.selector = selector;
    }

    /**
     * When you receive a message, print it out, then shut down the application.
     * Finally, clean up any ActiveMQ server stuff.
     */
    @JmsListener(destination = "${jms.queueName}", selector = "${jms.selector}")
    public void receiveMessage(MapMessage message) {
        if (timestampOfFirstMessage == 0) {
            timestampOfFirstMessage = System.currentTimeMillis();
            timestampLastBuffer = timestampOfFirstMessage;
        }
        messageReceived++;
        if (messageReceived % BUF_SIZE == 0) {
            messagesPerSecond = Application.calculateMessagesPerSeconds(timestampOfFirstMessage, messageReceived);
            messagesPerSecondLastBuffer = Application.calculateMessagesPerSeconds(timestampLastBuffer, BUF_SIZE);
            timestampLastBuffer = System.currentTimeMillis();
        }
        logger.debug("Received msg {} for selector {}, {} msg/s (cur: {} msg/s)",
                messageReceived, selector, messagesPerSecond, messagesPerSecondLastBuffer);
    }
}
