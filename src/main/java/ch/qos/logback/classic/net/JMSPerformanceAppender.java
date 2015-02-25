package ch.qos.logback.classic.net;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Map;

public class JMSPerformanceAppender extends JMSQueueAppender {

    private static final Logger LOG = LoggerFactory.getLogger(JMSPerformanceAppender.class);

    /**
     * This method called by {@link ch.qos.logback.core.AppenderBase#doAppend} method to do most
     * of the real appending work.
     */
    public void append(ILoggingEvent event) {
        if (!isStarted()) {
            LOG.debug("JMSPerformanceAppender hasn't been stated, skipping");
            return;
        }

        try {
            LOG.debug("Sending message with JMSPerformanceAppender");
            MapMessage msg = getQueueSession().createMapMessage();
            msg.setLong("timestamp", event.getTimeStamp());
            msg.setString("status", event.getArgumentArray()[0].toString());
            msg.setString("duration", event.getArgumentArray()[1].toString());
            if(event.getArgumentArray().length==3) {
                msg.setString("signature", event.getArgumentArray()[2].toString());
            }

            for(Map.Entry<String,String> mdc : event.getMDCPropertyMap().entrySet()) {
                msg.setString(mdc.getKey(), mdc.getValue());
            }
            queueSender.send(msg);
            successiveFailureCount = 0;
        } catch (Exception e) {
            successiveFailureCount++;
            if (successiveFailureCount > SUCCESSIVE_FAILURE_LIMIT) {
                stop();
            }
            addError("Could not send message in JMSQueueAppender [" + name + "].", e);
            LOG.error("Error sending message: {}", e);
        }
    }


}
