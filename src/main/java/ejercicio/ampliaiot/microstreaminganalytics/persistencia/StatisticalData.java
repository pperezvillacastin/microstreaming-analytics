package ejercicio.ampliaiot.microstreaminganalytics.persistencia;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@Document
public class StatisticalData {
    @Id
    private String id;
    private String measuredProperty;
    private Date readTime;
    private Double media;
    private Double mediana;
    private Set<Double> moda;
    private Double stddev;
    private List<Double> cuartiles;
    private Double maxVal;
    private Double minVal;

}
