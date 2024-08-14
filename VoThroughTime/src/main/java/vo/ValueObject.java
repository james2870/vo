package vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

abstract public class ValueObject {
    private String id;
    protected List<ValueChanged> ValueChangedEventList = new ArrayList<>();
    protected QualityOfData dataQuality = QualityOfData.qdQuestionable;
    protected Instant lastUpdate = Instant.EPOCH;  // 초기값 UTC 기준시 1970년 1월 1일 0시 0분 0초
    protected String value;
    private DataType dataType = DataType.dtNone;
    private QualityOfData sourceDataQuality = QualityOfData.qdNone;
    private Instant sourceDataLastUpdate = Instant.EPOCH;
    private BigDecimal deadBand = BigDecimal.ZERO;

    /**
     * ValueObject Class 생성자
     * @param id ValueObject의 Key 고유식별자. Not Register No.
     */
    public ValueObject(String id) {
        this.id = id;
    }

    /**
     * 고유식별자 읽기
     * @return id
     */
    public String getId() {
        return id;
    }

    public void addIOChangedEventListener(ValueChanged valueChanged) {
        ValueChangedEventList.add(valueChanged);
    }

    /**
     * 현재 Process에서의 Data Quality를 반환한다.
     * 선행 데이터 수집 Process의 Data Quality는 후행 데이터 처리 Process의 Data Quality에 영향을 미친다.
     * getDataQuality는 Questionable로 변경해야 함을 판단하며 dataQuality는 이는 setSourceDataQuality와 setValue를 통해 설정된다.
     * @return dataQuality
     */
    public QualityOfData getDataQuality() {
        if ((dataQuality == QualityOfData.qdGood) & (lastUpdate.isBefore(Instant.now().minus(1, ChronoUnit.HOURS)))) {
            // Data Quality가 Good 이지만, 최종 갱신 시간이 1시간 이전이라면 믿을수 없음으로 변경한다.
            dataQuality = QualityOfData.qdQuestionable;
        }
        return dataQuality;
    }

    /**
     * 최종 데이터 갱신시간을 의미한다. 이는 setValue에서 설정한다. readOnly
     * @return 최종 갱신 시간
     */
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public abstract void setValue(String value);

    /**
     * ValueObject의 Value
     * @return Value Object의 값을 String으로 반환한다.
     */
    public String getValue() {
        return value;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataQuality(QualityOfData dataQuality) {
        this.dataQuality = dataQuality;
    }

    public QualityOfData getSourceDataQuality() {
        return sourceDataQuality;
    }

    public Instant getSourceDataLastUpdate() {
        return sourceDataLastUpdate;
    }

    public void setDeadBand(BigDecimal deadBand) {
        this.deadBand = deadBand;
    }

    public BigDecimal getDeadBand() {
        return deadBand;
    }

    public abstract String getKeyValueJsonString();
}
