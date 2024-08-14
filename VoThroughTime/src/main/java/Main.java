import collection.*;
import vo.DataType;
import vo.ValueObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static collection.TimeSeriesSpan.tsHour;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        VOTimeDB voTimeDB = new VOTimeDB();
        ValueObject vo1 = new ValueObject("t1") {
            @Override
            public void setValue(String value) {
                lastUpdate = Instant.now();
                this.value = value;
            }

            @Override
            public String getKeyValueJsonString() {
                return null;
            }
        };
        vo1.setValue("1000");
        vo1.setDataType(DataType.dtDecimal);
        Instant baseTime = Instant.now();

        ValueObject vo2 = new ValueObject("t1") {
            @Override
            public void setValue(String value) {
                lastUpdate = Instant.now().plus(30, ChronoUnit.MINUTES);
                this.value = value;
            }

            @Override
            public String getKeyValueJsonString() {
                return null;
            }
        };
        vo2.setValue("1500");
        vo1.setDataType(DataType.dtDecimal);
        Thread.sleep(1000);

        ValueObject vo3 = new ValueObject("t1") {
            @Override
            public void setValue(String value) {
                lastUpdate = Instant.now().plus(45, ChronoUnit.MINUTES);
                this.value = value;
            }

            @Override
            public String getKeyValueJsonString() {
                return null;
            }
        };
        vo3.setValue("2000");
        vo3.setDataType(DataType.dtDecimal);

        voTimeDB.putVO((vo1));
        voTimeDB.putVO((vo2));
        voTimeDB.putVO((vo3));


        System.out.printf("Sum : %.0f\n", voTimeDB.getSum("t1", baseTime, tsHour));

        System.out.printf("Average : %.0f\n", voTimeDB.getAverage("t1", baseTime, tsHour));

        System.out.printf("Difference: %.0f\n", voTimeDB.getDifference("t1", baseTime, tsHour));
    }
}
