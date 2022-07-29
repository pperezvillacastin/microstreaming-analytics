package ejercicio.ampliaiot.microstreaminganalytics;

import com.rabbitmq.client.*;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalData;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalDataRepository;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Component
@Log
public class ScheduledRabbitMQReceiver {
    @Autowired
    StatisticalDataRepository statisticalDataRepository;

    @Value("${spring.rabbitmq.host}")
    private String host ;
    @Value("${spring.rabbitmq.port}")
    private String port ;
    @Value("${spring.rabbitmq.username}")
    private String username ;
    @Value("${spring.rabbitmq.password}")
    private String password ;
    @Value("${rabbitmq.consumer.queuename}")
    private String queue ;


    @Scheduled(fixedRate =5000)
    public void readQueue() throws IOException, TimeoutException {
        log.info("Start");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(Integer.valueOf(port));
        factory.setUsername(username);
        factory.setPassword(password);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(queue,false,false,false,null);

        while (declareOk.getMessageCount()>0) {
            GetResponse getResponse = channel.basicGet(queue,true);
            byte[] jsonBody = getResponse.getBody();
            //JSONObject testV=new JSONObject(new String(responseBody));
        }
        //WRITE  INTO MONGODB
        // TODO - Write actual results form the queue
        StatisticalData statisticalData = new StatisticalData();
        statisticalData.setId(RandomStringUtils.randomAlphabetic(8));
        statisticalData.setMedia(RandomUtils.nextDouble());
        StatisticalData result = statisticalDataRepository.insert(statisticalData);
        log.info("Basura en mongodb " + result);
    }
}
