package rs.ac.uns.ftn.informatika.jpa.agency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Profile;

// console-only app (no web)
@SpringBootApplication(scanBasePackages = "rs.ac.uns.ftn.informatika.jpa.agency")
@EnableRabbit
@Profile("agency")
public class AgencyApplication {
    public static void main(String[] args) {
        System.setProperty("spring.main.web-application-type", "none"); // console app
        SpringApplication app = new SpringApplication(AgencyApplication.class);
        app.setAdditionalProfiles("agency"); // <-- aktiviraj profil
        System.setProperty(
                "spring.rabbitmq.uri",
                "amqps://wyvanzbe:t_gF8mkk4Tv029I83APEIHGbSW1b-es5@kebnekaise.lmq.cloudamqp.com/wyvanzbe"
        );
        app.run(args);
    }
}
