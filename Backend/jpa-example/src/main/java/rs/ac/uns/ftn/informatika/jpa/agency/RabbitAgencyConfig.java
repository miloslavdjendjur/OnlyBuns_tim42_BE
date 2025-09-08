package rs.ac.uns.ftn.informatika.jpa.agency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit
@Profile("agency")
public class RabbitAgencyConfig {

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        cf.setHost("kebnekaise.lmq.cloudamqp.com");
        cf.setPort(5671);
        cf.setUsername("wyvanzbe");
        cf.setPassword("t_gF8mkk4Tv029I83APEIHGbSW1b-es5");
        cf.setVirtualHost("wyvanzbe");
        cf.useSslProtocol();   // TLS
        return new CachingConnectionFactory(cf);
    }

    @Bean
    public FanoutExchange adsFanoutExchange() {
        return new FanoutExchange("ads.fanout", true, false);
    }

    @Bean
    public AnonymousQueue ephemeralQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding bind(AnonymousQueue q, FanoutExchange ex) {
        return BindingBuilder.bind(q).to(ex);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(om);
    }

    // Default factory koji @RabbitListener koristi
    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(connectionFactory);
        f.setMessageConverter(converter);
        return f;
    }
}
