package rs.ac.uns.ftn.informatika.jpa.agency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import rs.ac.uns.ftn.informatika.jpa.dto.AdPostMessage;

import java.nio.charset.StandardCharsets;

@Component
@Profile("agency")
@RabbitListener(queues = "#{ephemeralQueue.name}") // klasa-lvl listener
public class AdsListener {

    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    // 1) Kada poruka dođe kao JSON (content-type=application/json)
    @RabbitHandler
    public void onMessage(AdPostMessage msg) {
        String inst = System.getProperty("instance", "1");
        System.out.printf("[AGENCY %s] user: %s | at: %s | desc: %s%n",
                inst, msg.getUsername(), msg.getPublishedAt(), msg.getDescription());
    }

    // 2) Fallback kada content-type fali (konzola u LavinMQ)
    @RabbitHandler
    public void onMessage(byte[] raw) {
        try {
            AdPostMessage msg = om.readValue(new String(raw, StandardCharsets.UTF_8), AdPostMessage.class);
            onMessage(msg);
        } catch (Exception e) {
            System.out.println("[AGENCY] Unsupported payload: " + new String(raw, StandardCharsets.UTF_8));
            e.printStackTrace();
        }
    }
}
