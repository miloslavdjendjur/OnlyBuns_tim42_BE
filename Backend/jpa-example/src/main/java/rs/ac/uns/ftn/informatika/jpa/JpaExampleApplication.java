package rs.ac.uns.ftn.informatika.jpa;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JpaExampleApplication {

	@Bean
	public ModelMapper getModelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		// >>> FORSIRAJ CloudAMQP ovde <<<
		System.setProperty(
				"spring.rabbitmq.uri",
				"amqps://wyvanzbe:t_gF8mkk4Tv029I83APEIHGbSW1b-es5@kebnekaise.lmq.cloudamqp.com/wyvanzbe"
		);

		SpringApplication.run(JpaExampleApplication.class, args);
	}

}
