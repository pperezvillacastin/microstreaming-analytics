package ejercicio.ampliaiot.microstreaminganalytics;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;

@RabbitListener(queues = "${rabbitmq.consumer.queuename}")
public class QueueReceiver {
    @RabbitHandler
    public void receive(String in) {
        System.out.println(" [x] Received '" + in + "'");
    }
}
