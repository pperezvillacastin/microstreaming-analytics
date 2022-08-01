package ejercicio.ampliaiot.microstreaminganalytics;

import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalData;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalDataRepository;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Log
@Component
public class StatisticalDataWriter {

    @Autowired
    StatisticalDataRepository statisticalDataRepository;


    public void dataListToStatisticalData(List<Double> dataList, String dataToExtract) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        dataList.stream().forEach(c->descriptiveStatistics.addValue(c));
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
    }

    // He intentado calcula la moda con apache commons pero me devolvia la lista
    // de entrada entera. Saco este otro ejemplo de Stack Overflow (sabria
    // calcularla para integers pero no para doubles)
    // https://stackoverflow.com/a/38937305
    // Generalmente va a devolver todos los elementos de entrada a no ser que deliberadamente
    // repitamos alguno
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
