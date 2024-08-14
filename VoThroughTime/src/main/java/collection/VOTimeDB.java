package collection;

import vo.ValueObject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

public class VOTimeDB {
    private HashMap<String, VOTimeSeries> voDB = new HashMap<>();

    public void putVO(ValueObject vo) {
        // HashMap에 id가 있는지 확인한다.
        VOTimeSeries voTimeSeries = voDB.get(vo.getId());

        // 없으면 만들어서 HashMap에 넣는다.
        if (voTimeSeries == null) {
            voTimeSeries = new VOTimeSeries();
            voDB.put(vo.getId(), voTimeSeries);
        }

        voTimeSeries.addVO(vo);
    }

    public BigDecimal getSum(String id, Instant baseTime, TimeSeriesSpan span) {
        VOTimeSeries voTimeSeries = voDB.get(id);
        if (voTimeSeries == null) return null;

        return voTimeSeries.getSum(baseTime, span);
    }

    public BigDecimal getAverage(String id, Instant baseTime, TimeSeriesSpan span) {
        VOTimeSeries voTimeSeries = voDB.get(id);
        if (voTimeSeries == null) return null;

        return voTimeSeries.getAverage(baseTime, span);
    }

    public BigDecimal getDifference(String id, Instant baseTime, TimeSeriesSpan span) {
        VOTimeSeries voTimeSeries = voDB.get(id);
        if (voTimeSeries == null) return null;

        return voTimeSeries.getDifference(baseTime, span);
    }


}
