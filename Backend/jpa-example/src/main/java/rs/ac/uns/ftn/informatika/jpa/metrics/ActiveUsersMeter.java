package rs.ac.uns.ftn.informatika.jpa.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ActiveUsersMeter {

    private final ConcurrentMap<String, Long> lastSeen = new ConcurrentHashMap<>();
    private final Clock clock = Clock.systemUTC();

    public ActiveUsersMeter(MeterRegistry registry) {
        Gauge.builder("active_users_gauge", this, ActiveUsersMeter::currentActive)
                .description("Number of users active in the last 15 minutes")
                .register(registry);
    }

    /** Pozovi ovo kad je korisnik uspešno autentifikovan / ima važeći token. */
    public void markActive(String userIdOrUsername) {
        lastSeen.put(userIdOrUsername, clock.millis());
    }

    /** Koliko ih je bilo aktivno u poslednjih 15 min. */
    public int currentActive() {
        long now = clock.millis();
        long windowMs = 15L * 60L * 1000L;
        long cutoff = now - windowMs;
        return (int) lastSeen.values().stream().filter(ts -> ts >= cutoff).count();
    }
}
