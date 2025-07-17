package rs.ac.uns.ftn.informatika.jpa.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.jpa.model.CareLocation;
import rs.ac.uns.ftn.informatika.jpa.model.LocationMessage;
import rs.ac.uns.ftn.informatika.jpa.service.CareLocationService;

@Component
public class CareLocationListener {

    private final CareLocationService service;

    public CareLocationListener(CareLocationService service) {
        this.service = service;
    }

    @RabbitListener(queues = "care-location-queue")
    public void receiveLocation(LocationMessage message) {
        System.out.println("📥 Primljena poruka: " + message.getNaziv() + " - " + message.getLokacija());
        CareLocation location = new CareLocation(message.getId(), message.getNaziv(), message.getLokacija());
        service.save(location);
    }

}
