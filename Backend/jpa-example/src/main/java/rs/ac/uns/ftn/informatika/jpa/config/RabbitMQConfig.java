package rs.ac.uns.ftn.informatika.jpa.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!agency") // ova konfiguracija važi SAMO za glavnu app
public class RabbitMQConfig {

    // FORCE CloudAMQP TLS konekciju (5671)
    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        cf.setHost("kebnekaise.lmq.cloudamqp.com");
        cf.setPort(5671);                 // TLS port
        cf.setUsername("wyvanzbe");
        cf.setPassword("t_gF8mkk4Tv029I83APEIHGbSW1b-es5");
        cf.setVirtualHost("wyvanzbe");
        cf.useSslProtocol();              // obavezno za TLS
        return new CachingConnectionFactory(cf);
    }

    // Exchange koji koriste reklame (isto ime i argumenti kao u agency)
    @Bean
    public FanoutExchange adsFanoutExchange() {
        return new FanoutExchange("ads.fanout", true, false); // durable=true, autoDelete=false
    }

    // 👇 DODATO: deklaracija queue-a koji koristi tvoj CareLocationListener
    @Bean
    public Queue careLocationQueue() {
        return new Queue("care-location-queue", true, false, false); // durable queue
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
