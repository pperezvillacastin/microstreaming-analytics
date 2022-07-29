package ejercicio.ampliaiot.microstreaminganalytics;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalData;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalDataRepository;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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

    //Esto no deberia estar hardcodeado
    private static final String dataToExtract = "flowrate";


    @Scheduled(fixedRateString = "${queue.read.rate}")
    public void readQueue() throws IOException, TimeoutException {
        try {
            log.info("Start");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(Integer.valueOf(port));
            factory.setUsername(username);
            factory.setPassword(password);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
            List<Double> dataList = new LinkedList<Double>();
            GetResponse getResponse = null;
            do {
                getResponse = channel.basicGet(queue, true);
                if (getResponse == null) break;
                String jsonBody = new String(getResponse.getBody());
                descriptiveStatistics.addValue(extractDataStreamSingleValue(jsonBody,dataToExtract));
                dataList.add(extractDataStreamSingleValue(jsonBody,dataToExtract));
            }
            while (getResponse != null);
            channel.close();
            connection.close();



            StatisticalData statisticalData = new StatisticalData(
                    RandomStringUtils.randomAlphabetic(12), //id
                    dataToExtract, //tipo de dato
                    new Date(), // timestamp
                    descriptiveStatistics.getMean(), //media
                    descriptiveStatistics.getPercentile(50), //mediana
                    getMode(dataList), // moda
                    descriptiveStatistics.getStandardDeviation(), //desviacion estandar
                    new ArrayList<Double>(Arrays.asList(
                            descriptiveStatistics.getPercentile(25),
                            descriptiveStatistics.getPercentile(50),
                            descriptiveStatistics.getPercentile(75)
                    )), //cuartiles
                    descriptiveStatistics.getMax(), // max
                    descriptiveStatistics.getMin() // min
            );
            StatisticalData result = statisticalDataRepository.insert(statisticalData);
            String listString = dataList.stream().map(Object::toString).collect(Collectors.joining(", "));
            log.info("data set:");
            log.info(listString);
            log.info("result set:");
            log.info(result.toString());
        } catch (Exception e) {
            log.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public Double extractDataStreamSingleValue(String json, String datastreamtype) throws Exception {
        //Simplemente voy a parsear el JSON de entrada en vez de intentar
        //serializarlo a un objeto

        JsonFactory factory = new JsonFactory();

        ObjectMapper mapper = new ObjectMapper(factory);
        JsonNode rootNode = mapper.readTree(json);
        JsonNode datastreams = rootNode.get("datastreams");
        //En un caso mas complicado habria que iterar sobre los elementos dentro de datastreams y recuperar
        //la informacion de cada uno de ellos pero en este caso
        //solo estoy enviando un elemento en la lista.
        // Tambien faltan muchos checks para validar el objeto de entrada

        //Entiendo ahora que a lo mejor la intencion original del ejercicio es que los
        // datos estadisticos se calculasen desde varias entradas en un mismo fichero
        // (que hubiese facilitado las cosas ya que hacer un consumidor en spring boot
        // que opere sobre un mensaje es bastante mas sencillo), pero el ejemplo
        // en el que me base (erroneamente) de la documentacion tenia un solo campo
        JsonNode datastream = datastreams.get(0);
        if (!datastream.get("id").textValue().equals(datastreamtype)  ){
            throw new Exception("Wrong stream type received " + datastream.get("id").textValue() + datastreamtype + " expected");
        }
        //Igual que antes, solo considero un data point por fichero (pero multiples elementos en la cola)
        return datastream.get("datapoints").get(0).get("value").doubleValue();
    }

    // He intentado calcula la moda con apache commons pero me devolvia la lista
    // de entrada entera. Saco este otro ejemplo de Stack Overflow (sabria
    // calcularla para integers pero no para doubles)
    // https://stackoverflow.com/a/38937305
    private static Set<Double> getMode(double[] data) {
        if (data.length == 0) {
            return new TreeSet<>();
        }
        TreeMap<Double, Integer> map = new TreeMap<>(); //Map Keys are array values and Map Values are how many times each key appears in the array
        for (int index = 0; index != data.length; ++index) {
            double value = data[index];
            if (!map.containsKey(value)) {
                map.put(value, 1); //first time, put one
            }
            else {
                map.put(value, map.get(value) + 1); //seen it again increment count
            }
        }
        Set<Double> modes = new TreeSet<>(); //result set of modes, min to max sorted
        int maxCount = 1;
        Iterator<Integer> modeApperance = map.values().iterator();
        while (modeApperance.hasNext()) {
            maxCount = Math.max(maxCount, modeApperance.next()); //go through all the value counts
        }
        for (double key : map.keySet()) {
            if (map.get(key) == maxCount) { //if this key's value is max
                modes.add(key); //get it
            }
        }
        return modes;
    }

    private static Set<Double> getMode(List<Double> dataList){
        return getMode(StatUtils.mode(dataList.stream().mapToDouble(Double::doubleValue).toArray()));
    }


}
