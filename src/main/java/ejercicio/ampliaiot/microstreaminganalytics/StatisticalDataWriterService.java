package ejercicio.ampliaiot.microstreaminganalytics;

import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalData;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalDataRepository;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Log
@Component
public class StatisticalDataWriterService {

    @Autowired
    StatisticalDataRepository statisticalDataRepository;


    public StatisticalData dataListToStatisticalData(List<Double> dataList, String dataToExtract) {
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
        String listString = dataList.stream().map(Object::toString).collect(Collectors.joining(", "));
        log.info("data set:");
        log.info(listString);
        log.info("result set:");
        log.info(statisticalData.toString());
        try {
            statisticalDataRepository.insert(statisticalData);
        }
        catch(Exception e) {
            log.severe("Database write failed " + ExceptionUtils.getStackTrace(e));
            throw e;
        }
        return statisticalData;

    }

    private static List<Double> getMode(double[] data) {
        //El problema que habia con este metodo es que la lista de datos era
        //demasiado pequeña y la precision de los doubles demasiado alta
        //como para obtener coincidencias.

        //Habiendo reducido los decimales de los doubles a uno
        // e incrementando la cantidad de datos por lectura a unos 300
        // ya no hay este problema y prefiero usar Apache Commons
        // a un resultado aleatorio de internet
        double[] modearray = StatUtils.mode(data);
        List<Double> modeList = new ArrayList<Double>();
        for (double primitivo : modearray) {
            modeList.add(primitivo);
        }
        return modeList;
    }

    private static List<Double> getMode(List<Double> dataList){
        return getMode(StatUtils.mode(dataList.stream().mapToDouble(Double::doubleValue).toArray()));
    }



}
