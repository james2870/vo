package collection;

import vo.ValueObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class TimeSlots {
    private BigDecimal[] values;
    private Instant baseTime;
    public TimeSlots(TimeSeriesSpan timeSeriesSpan) {
        switch (timeSeriesSpan) {
            case tsHour -> values = new BigDecimal[60];
            case tsDay -> values = new BigDecimal[1440];
        }
        for (BigDecimal bd : values) {
            bd = BigDecimal.ZERO;
        }
    }

    public void setBaseline(Instant baseTime, BigDecimal startValue) {
        this.baseTime = baseTime;
        values[0] = startValue;
    }

    public void addMiddleValue(Instant middleTime, BigDecimal middleValue) {
        long idx = ChronoUnit.MINUTES.between(baseTime, middleTime);
        values [(int) idx] = middleValue;
    }

    public BigDecimal[] getFilledValues() {
        for(int i=1; i<values.length; i++) {
            if (values[i] == null) {
                values[i] = values[i-1];
            }
        }
        return values;
    }
}

/**
 * VO의 LasUpdateTime 비교를 위한 Comparator
 */
class VOLastUpdateTimeComparator implements Comparator<ValueObject> {

    @Override
    public int compare(ValueObject o1, ValueObject o2) {
        if (o1.getLastUpdate().isAfter(o2.getLastUpdate()))
            return 1;
        else if (o1.getLastUpdate().isBefore(o2.getLastUpdate()))
            return -1;
        else
            return 0;
    }
}

public class VOTimeSeries extends ArrayList<ValueObject>
{
    //todo VO 수명관리 할 것.
    public void addVO(ValueObject vo) {
        // List가 비었으면 그냥 넣는다.
        if (this.isEmpty()) {
            this.add(vo);
            return;
        }

        // List가 비지 않았다면 마지막 VO의 시간과 현재 입력 VO의 시간을 비교해 본다. 항상 정렬된 상태로 유지한다.
        // 현재 입력되는 VO의 시간이 이전 VO의 시간보다 앞선다면 정렬이 필요하다.
        boolean needSort = false;
        ValueObject lastValueObject = this.get(this.size()-1);
        if (lastValueObject.getLastUpdate().isAfter(vo.getLastUpdate()))
            needSort = true;

        this.add(vo);

        if (needSort) {
            Collections.sort(this, new VOLastUpdateTimeComparator());
        }

        // 지금부터 48간 전의 VO Index를 검색하고 삭제한다.
        int delIdx = searchIndexfromLastUpdate(Instant.now().minus(2,ChronoUnit.DAYS));
        if (delIdx != -1) {
            for(int i=delIdx; i>=0; i--) {
                this.remove(i);
            }
        }
    }

    public BigDecimal getSum(Instant baseTime, TimeSeriesSpan span) {
// Series 가 비었으면 오류를 반환한다.
        if (this.isEmpty()) return null;

        //초기값 Index 확인
        int baseIdx = searchIndexfromLastUpdate(baseTime);
        //마지막 값 Index 확인
        int endIdx = -1;
        if (span == TimeSeriesSpan.tsHour)
            endIdx = searchIndexfromLastUpdate(baseTime.plus(1, ChronoUnit.HOURS));
        else if (span == TimeSeriesSpan.tsDay)
            endIdx = searchIndexfromLastUpdate(baseTime.plus(1, ChronoUnit.DAYS));

        // 첫번째 데이터의 값이 원하는 만큼 시간이 없다면 오류를 반환한다.
        if (baseIdx == -1) return null;
        if (endIdx == -1) return null;

        //계산을 위한 TimeSlots 생성
        TimeSlots timeSlots = new TimeSlots(span);

        // TimeSlot에 초기값 입력
        ValueObject vo = this.get(baseIdx);
        timeSlots.setBaseline(vo.getLastUpdate(), new BigDecimal(vo.getValue()));

        // Middle 값 입력
        for(int i=baseIdx+1; i<=endIdx; i++) {
            vo = this.get(i);
            timeSlots.addMiddleValue(vo.getLastUpdate(), new BigDecimal(vo.getValue()));
        }
        BigDecimal[] bd = timeSlots.getFilledValues();
        BigDecimal sum = BigDecimal.ZERO;
        for(int i = 0; i<bd.length; i++) {
            sum = sum.add(bd[i]);
//            System.out.printf("%d, %.0f\n", i, bd[i]);
        }
        return sum;
    }

    public BigDecimal getAverage(Instant baseTime, TimeSeriesSpan span) {
        BigDecimal sum = getSum(baseTime, span);
        if (sum == null) return null;

        if (span == TimeSeriesSpan.tsHour)
            return sum.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        else if (span == TimeSeriesSpan.tsDay)
            return sum.divide(BigDecimal.valueOf(1440), 2, RoundingMode.HALF_UP);
        else
            return null;
    }

    public BigDecimal getDifference(Instant baseTime, TimeSeriesSpan span) {
// Series 가 비었으면 오류를 반환한다.
        if (this.isEmpty()) return null;

        //초기값 Index 확인
        int baseIdx = searchIndexfromLastUpdate(baseTime);
        //마지막 값 Index 확인
        int endIdx = -1;
        if (span == TimeSeriesSpan.tsHour)
            endIdx = searchIndexfromLastUpdate(baseTime.plus(1, ChronoUnit.HOURS));
        else if (span == TimeSeriesSpan.tsDay)
            endIdx = searchIndexfromLastUpdate(baseTime.plus(1, ChronoUnit.DAYS));

        // 첫번째 데이터의 값이 원하는 만큼 시간이 없다면 오류를 반환한다.
        if (baseIdx == -1) return null;
        if (endIdx == -1) return null;

        BigDecimal endValue = new BigDecimal(this.get(endIdx).getValue());
        BigDecimal baseValue = new BigDecimal(this.get(baseIdx).getValue());

        return endValue.subtract(baseValue);
    }

    /**
     * 요청한 updateTime 바로 직전 혹은 같은 VO의 Index를 반환한다.
     * @param updateTime : 기준 시간
     * @return 검색된 직전 Index
     */
    private int searchIndexfromLastUpdate(Instant updateTime) {
        int lastIndex = -1;
        for (int i = 0; i<this.size(); i++) {
            ValueObject vo = this.get(i);
            // vo의 LastUpdate가 기준시간과 같으면 현재 Index를 보낸다.
            if (vo.getLastUpdate().equals(updateTime)) {
                lastIndex = i;
                break;
            }
            // 나중이면 이전 local variable lastIndex가 반환되도록 하여 i-1 이 반환되도록 한다.
            if ((vo.getLastUpdate().isAfter(updateTime)))
                break;

            lastIndex = i;
        }
        return lastIndex;
    }

    // todo delete methode for test
    public void printList() {
        for (ValueObject vo :
                this) {
            System.out.printf("value %s, datetime %s\n", vo.getValue(), vo.getLastUpdate().toString());
        }

        TimeSlots timeSlots = new TimeSlots(TimeSeriesSpan.tsHour);
        ValueObject vo = this.get(0);
        timeSlots.setBaseline(vo.getLastUpdate(), new BigDecimal(vo.getValue()));
        vo = this.get(1);
        timeSlots.addMiddleValue(vo.getLastUpdate(), new BigDecimal(vo.getValue()));
        vo = this.get(2);
        timeSlots.addMiddleValue(vo.getLastUpdate(), new BigDecimal(vo.getValue()));
        BigDecimal[] bd = timeSlots.getFilledValues();
        BigDecimal sum = BigDecimal.ZERO;
        for(int i = 0; i<bd.length; i++) {
            sum = sum.add(bd[i]);
            System.out.printf("%d, %.0f\n", i, bd[i]);
        }
        System.out.printf("SUM : %.0f\n", sum);
        System.out.printf("AVG : %.2f\n", sum.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
    }
}
