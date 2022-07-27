package ejercicio.ampliaiot.microstreaminganalytics;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicrostreamingAnalyticsConfig {
    @Value("${rabbitmq.consumer.queuename")
    private String queueName;
    @Bean
    public Queue microstreamingAnalytics() {
        return new Queue(queueName);
    }
    @Bean
    public QueueReceiver receiver() {
        return new QueueReceiver();
    }
}
