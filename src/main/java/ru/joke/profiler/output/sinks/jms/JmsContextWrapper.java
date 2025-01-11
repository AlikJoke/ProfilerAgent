package ru.joke.profiler.output.sinks.jms;

import jakarta.jms.*;
import ru.joke.profiler.output.sinks.util.pool.AbstractPooledConnection;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

final class JmsContextWrapper extends AbstractPooledConnection implements JMSContext {

    private static final Logger logger = Logger.getLogger(JmsContextWrapper.class.getCanonicalName());

    private final Supplier<JMSContext> connectionRetriever;

    private volatile JMSContext connection;
    private volatile JMSProducer producer;

    JmsContextWrapper(final Supplier<JMSContext> connectionRetriever) {
        this.connectionRetriever = connectionRetriever;
    }

    @Override
    public synchronized boolean init() {
        try {
            if (this.connection == null) {
                this.connection = connectionRetriever.get();
            }

            return super.init();
        } catch (JMSRuntimeException ex) {
            logger.log(Level.SEVERE, "Unable to init jms connection", ex);
            return false;
        }
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return this.connection.createContext(sessionMode);
    }

    @Override
    public JMSProducer createProducer() {
        JMSProducer producer = this.producer;
        if (producer == null) {
            this.producer = producer = this.connection.createProducer();
        }

        return producer;
    }

    @Override
    public String getClientID() {
        return this.connection.getClientID();
    }

    @Override
    public void setClientID(String clientID) {
        this.connection.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return this.connection.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return this.connection.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        this.connection.setExceptionListener(listener);
    }

    @Override
    public void start() {
        this.connection.start();
    }

    @Override
    public void stop() {
        this.connection.stop();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        this.connection.setAutoStart(autoStart);
    }

    @Override
    public boolean getAutoStart() {
        return this.connection.getAutoStart();
    }

    @Override
    public BytesMessage createBytesMessage() {
        return this.connection.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return this.connection.createMapMessage();
    }

    @Override
    public Message createMessage() {
        return this.connection.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return this.connection.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return this.connection.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return this.connection.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return this.connection.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return this.connection.createTextMessage(text);
    }

    @Override
    public boolean getTransacted() {
        return this.connection.getTransacted();
    }

    @Override
    public int getSessionMode() {
        return this.connection.getSessionMode();
    }

    @Override
    public void commit() {
        this.connection.commit();
    }

    @Override
    public void rollback() {
        this.connection.rollback();
    }

    @Override
    public void recover() {
        this.connection.recover();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return this.connection.createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return this.connection.createConsumer(destination, messageSelector);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return this.connection.createConsumer(destination, messageSelector, noLocal);
    }

    @Override
    public Queue createQueue(String queueName) {
        return this.connection.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return this.connection.createTopic(topicName);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return this.connection.createDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return this.connection.createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return this.connection.createSharedDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return this.connection.createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return this.connection.createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return this.connection.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return this.connection.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return this.connection.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return this.connection.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return this.connection.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        this.connection.unsubscribe(name);
    }

    @Override
    public void acknowledge() {
        this.connection.acknowledge();
    }

    @Override
    public synchronized void close() {
        if (this.connection == null) {
            return;
        }

        final JMSContext connection = this.connection;
        this.connection = null;
        this.producer = null;
        try {
            connection.close();
        } catch (JMSRuntimeException ex) {
            logger.log(Level.WARNING, "Unable to close JMS session", ex);
        }
    }
}