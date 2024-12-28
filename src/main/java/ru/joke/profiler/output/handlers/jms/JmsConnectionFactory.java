package ru.joke.profiler.output.handlers.jms;

import jakarta.jms.JMSContext;
import ru.joke.profiler.output.handlers.ProfilerOutputSinkException;
import ru.joke.profiler.output.handlers.util.pool.ConnectionFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

final class JmsConnectionFactory implements ConnectionFactory<JmsContextWrapper> {

    private final jakarta.jms.ConnectionFactory connectionFactory;

    JmsConnectionFactory(final JmsSinkConfiguration.OutputDestinationConfiguration configuration) {
        this.connectionFactory = lookup(configuration.connectionFactoryJndiName());
    }

    @Override
    public JmsContextWrapper create() {
        return new JmsContextWrapper(() -> this.connectionFactory.createContext(JMSContext.CLIENT_ACKNOWLEDGE));
    }

    private jakarta.jms.ConnectionFactory lookup(final String jndiName) {
        try {
            final InitialContext initialContext = new InitialContext();
            return (jakarta.jms.ConnectionFactory) initialContext.lookup(jndiName);
        } catch (NamingException ex) {
            throw new ProfilerOutputSinkException(ex);
        }
    }
}
