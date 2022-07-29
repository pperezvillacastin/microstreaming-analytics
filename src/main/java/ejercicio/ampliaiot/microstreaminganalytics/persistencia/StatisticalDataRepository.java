package ejercicio.ampliaiot.microstreaminganalytics.persistencia;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticalDataRepository extends MongoRepository<StatisticalData,String> {
}
