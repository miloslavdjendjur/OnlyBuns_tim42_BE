package rs.ac.uns.ftn.informatika.jpa.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessage;

@Service
public class AdvertisingService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public AdvertisingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(AdPostMessage msg) {
        // routingKey is ignored for fanout exchanges
        rabbitTemplate.convertAndSend("ads.fanout", "", msg);
    }
}