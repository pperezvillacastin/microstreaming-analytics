package ejercicio.ampliaiot.microstreaminganalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MicrostreamingAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrostreamingAnalyticsApplication.class, args);
	}

}
