package ejercicio.ampliaiot.microstreaminganalytics;

import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalData;
import ejercicio.ampliaiot.microstreaminganalytics.persistencia.StatisticalDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class MockRepositoryTest {
    @MockBean(name ="statisticalDataRepository")
    private StatisticalDataRepository statisticalDataRepository;
    @Autowired
    StatisticalDataWriterService statisticalDataWriterService;

    @Test
    public void statisticalDataWriter() {
        List<Double> inputList = Arrays.asList(new Double[]{1.0,20.0,40.0,20.0,33.33,55.55,7.7,20.0,33.33,55.55,7.7});
        StatisticalData statisticalData = statisticalDataWriterService.dataListToStatisticalData(inputList, "whatever");
        Assertions.assertEquals(statisticalData.getModa().get(0), 20.0);
        Assertions.assertEquals(statisticalData.getStddev(), 18.564992764976676);
    }

}
