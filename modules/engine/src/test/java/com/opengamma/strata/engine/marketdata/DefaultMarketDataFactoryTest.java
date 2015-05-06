package com.opengamma.strata.engine.marketdata;

import static com.opengamma.strata.collect.CollectProjectAssertions.assertThat;
import static com.opengamma.strata.collect.Guavate.toImmutableMap;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.index.IborIndices;
import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.collect.id.StandardId;
import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.engine.calculations.MissingMappingId;
import com.opengamma.strata.engine.calculations.NoMatchingRuleId;
import com.opengamma.strata.engine.marketdata.builders.DiscountingCurveMarketDataBuilder;
import com.opengamma.strata.engine.marketdata.builders.IndexCurveMarketDataBuilder;
import com.opengamma.strata.engine.marketdata.builders.MarketDataBuilder;
import com.opengamma.strata.engine.marketdata.builders.ObservableMarketDataBuilder;
import com.opengamma.strata.engine.marketdata.builders.TimeSeriesProvider;
import com.opengamma.strata.engine.marketdata.mapping.FeedIdMapping;
import com.opengamma.strata.marketdata.curve.CurveGroup;
import com.opengamma.strata.marketdata.id.CurveGroupId;
import com.opengamma.strata.marketdata.id.DiscountingCurveId;
import com.opengamma.strata.marketdata.id.FieldName;
import com.opengamma.strata.marketdata.id.IndexCurveId;
import com.opengamma.strata.marketdata.id.IndexRateId;
import com.opengamma.strata.marketdata.id.MarketDataFeed;
import com.opengamma.strata.marketdata.id.MarketDataId;
import com.opengamma.strata.marketdata.id.ObservableId;
import com.opengamma.strata.marketdata.id.QuoteId;
import com.opengamma.strata.marketdata.key.DiscountingCurveKey;

@Test
public class DefaultMarketDataFactoryTest {

  private static final MarketDataFeed VENDOR = MarketDataFeed.of("RealFeed");
  private static final IndexRateId LIBOR_1M_ID = IndexRateId.of(IborIndices.USD_LIBOR_1M, VENDOR);
  private static final IndexRateId LIBOR_3M_ID = IndexRateId.of(IborIndices.USD_LIBOR_3M, VENDOR);

  /**
   * Tests building time series from requirements.
   */
  public void buildTimeSeries() {
    IborIndex chfIndex = IborIndices.CHF_LIBOR_1M;
    IborIndex eurIndex = IborIndices.EUR_LIBOR_1M;
    IndexRateId chfId = IndexRateId.of(chfIndex);
    IndexRateId eurId = IndexRateId.of(eurIndex);
    LocalDateDoubleTimeSeries chfTimeSeries =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2011, 3, 8), 1)
            .put(date(2011, 3, 9), 2)
            .put(date(2011, 3, 10), 3)
            .build();
    LocalDateDoubleTimeSeries eurTimeSeries =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2012, 4, 8), 10)
            .put(date(2012, 4, 9), 20)
            .put(date(2012, 4, 10), 30)
            .build();
    Map<IndexRateId, LocalDateDoubleTimeSeries> timeSeries = ImmutableMap.of(chfId, chfTimeSeries, eurId, eurTimeSeries);
    DefaultMarketDataFactory marketDataFactory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(timeSeries),
            ObservableMarketDataBuilder.none(),
            FeedIdMapping.identity());

    MarketDataRequirements requirements =
        MarketDataRequirements.builder()
            .addTimeSeries(chfId, eurId)
            .build();
    BaseMarketData marketData =
        marketDataFactory.buildBaseMarketData(requirements, BaseMarketData.empty(date(2015, 3, 25))).getMarketData();

    assertThat(marketData.getTimeSeries(chfId)).isEqualTo(chfTimeSeries);
    assertThat(marketData.getTimeSeries(eurId)).isEqualTo(eurTimeSeries);
  }

  /**
   * Tests building single values using market data builders.
   */
  public void buildNonObservableValues() {
    CurveGroup curveGroup = MarketDataTestUtils.curveGroup();
    YieldCurve discountingCurve = MarketDataTestUtils.discountingCurve(1, Currency.AUD, curveGroup);
    YieldCurve iborCurve = MarketDataTestUtils.iborIndexCurve(1, IborIndices.EUR_EURIBOR_12M, curveGroup);
    DiscountingCurveId discountingCurveId = DiscountingCurveId.of(Currency.AUD, MarketDataTestUtils.CURVE_GROUP_NAME);
    IndexCurveId iborCurveId = IndexCurveId.of(IborIndices.EUR_EURIBOR_12M, MarketDataTestUtils.CURVE_GROUP_NAME);
    CurveGroupId groupId = CurveGroupId.of(MarketDataTestUtils.CURVE_GROUP_NAME);
    BaseMarketData suppliedData = BaseMarketData.builder(date(2011, 3, 8)).addValue(groupId, curveGroup).build();
    DefaultMarketDataFactory marketDataFactory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            ObservableMarketDataBuilder.none(),
            FeedIdMapping.identity(),
            new DiscountingCurveMarketDataBuilder(),
            new IndexCurveMarketDataBuilder());

    MarketDataRequirements requirements =
        MarketDataRequirements.builder()
            .addValues(discountingCurveId, iborCurveId)
            .build();
    MarketDataResult result = marketDataFactory.buildBaseMarketData(requirements, suppliedData);
    BaseMarketData marketData = result.getMarketData();
    assertThat(marketData.getValue(discountingCurveId)).isEqualTo(discountingCurve);
    assertThat(marketData.getValue(iborCurveId)).isEqualTo(iborCurve);
  }

  /**
   * Tests building observable market data values.
   */
  public void buildObservableValues() {
    DefaultMarketDataFactory factory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            new TestObservableMarketDataBuilder(),
            new TestFeedIdMapping());

    BaseMarketData suppliedData = BaseMarketData.empty(date(2011, 3, 8));
    QuoteId id1 = QuoteId.of(StandardId.of("reqs", "a"));
    QuoteId id2 = QuoteId.of(StandardId.of("reqs", "b"));
    MarketDataRequirements requirements = MarketDataRequirements.builder().addValues(id1, id2).build();
    BaseMarketData marketData = factory.buildBaseMarketData(requirements, suppliedData).getMarketData();

    assertThat(marketData.getValue(id1)).isEqualTo(1d);
    assertThat(marketData.getValue(id2)).isEqualTo(2d);
  }

  /**
   * Tests that failures are included in the results for keys with no mapping.
   */
  public void missingMapping() {
    DefaultMarketDataFactory factory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            new TestObservableMarketDataBuilder(),
            new TestFeedIdMapping());

    DiscountingCurveKey key = DiscountingCurveKey.of(Currency.GBP);
    MissingMappingId missingId = MissingMappingId.of(key);
    MarketDataRequirements requirements = MarketDataRequirements.builder().addValues(missingId).build();
    MarketDataResult result = factory.buildBaseMarketData(requirements, BaseMarketData.empty(date(2011, 3, 8)));
    Map<MarketDataId<?>, Result<?>> failures = result.getSingleValueFailures();
    Result<?> missingResult = failures.get(missingId);

    assertThat(missingResult).isFailure();
    String message = Messages.format("No market data mapping found for market data key {}", key);
    assertThat(missingResult.getFailure().getMessage()).isEqualTo(message);
  }

  /**
   * Tests that failures are included in the results for observable market data when there is no
   * matching market data rule for a calculation.
   */
  public void noMatchingMarketDataRuleObservables() {
    IndexRateId libor6mId = IndexRateId.of(IborIndices.USD_LIBOR_6M, MarketDataFeed.NO_RULE);
    IndexRateId libor12mId = IndexRateId.of(IborIndices.USD_LIBOR_12M, MarketDataFeed.NO_RULE);

    Set<IndexRateId> requirements = ImmutableSet.of(libor6mId, libor12mId, LIBOR_1M_ID, LIBOR_3M_ID);

    DefaultMarketDataFactory factory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            new DelegateBuilder(),
            Optional::of);

    MarketDataRequirements marketDataRequirements = MarketDataRequirements.builder().addValues(requirements).build();
    MarketDataResult result =
        factory.buildBaseMarketData(marketDataRequirements, BaseMarketData.empty(date(2011, 3, 8)));
    Map<MarketDataId<?>, Result<?>> failures = result.getSingleValueFailures();
    BaseMarketData marketData = result.getMarketData();

    assertThat(marketData.getValue(LIBOR_1M_ID)).isEqualTo(1d);
    assertThat(marketData.getValue(LIBOR_3M_ID)).isEqualTo(3d);
    assertThat(failures.get(libor6mId)).hasFailureMessageMatching("No market data rule.*");
    assertThat(failures.get(libor12mId)).hasFailureMessageMatching("No market data rule.*");
  }

  /**
   * Tests that failures are included in the results for non-observable market data when there is no matching
   * market data rule for a calculation.
   */
  public void noMatchingMarketDataRuleNonObservables() {
    DiscountingCurveKey gbpCurveKey = DiscountingCurveKey.of(Currency.GBP);
    NoMatchingRuleId gbpCurveId = NoMatchingRuleId.of(gbpCurveKey);
    DiscountingCurveId eurCurveId = DiscountingCurveId.of(Currency.EUR, "curve group");

    DefaultMarketDataFactory factory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            new TestObservableMarketDataBuilder(),
            Optional::of,
            new CurveBuilder());

    MarketDataRequirements requirements = MarketDataRequirements.builder().addValues(eurCurveId, gbpCurveId).build();
    MarketDataResult result = factory.buildBaseMarketData(requirements, BaseMarketData.empty(date(2011, 3, 8)));
    Map<MarketDataId<?>, Result<?>> singleValueFailures = result.getSingleValueFailures();
    BaseMarketData marketData = result.getMarketData();

    assertThat(singleValueFailures.get(gbpCurveId)).hasFailureMessageMatching("No market data rule.*");
    assertThat(marketData.getValue(eurCurveId)).isNotNull();
  }

  /**
   * Tests that failures are included in the results for time series when there is no matching
   * market data rule for a calculation.
   */
  public void noMatchingMarketDataRuleTimeSeries() {
    IndexRateId libor6mId = IndexRateId.of(IborIndices.USD_LIBOR_6M, MarketDataFeed.NO_RULE);
    IndexRateId libor12mId = IndexRateId.of(IborIndices.USD_LIBOR_12M, MarketDataFeed.NO_RULE);
    Set<IndexRateId> requirements = ImmutableSet.of(libor6mId, libor12mId, LIBOR_1M_ID, LIBOR_3M_ID);

    LocalDateDoubleTimeSeries libor1mTimeSeries =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2011, 3, 8), 1d)
            .put(date(2011, 3, 9), 2d)
            .put(date(2011, 3, 10), 3d)
            .build();

    LocalDateDoubleTimeSeries libor3mTimeSeries =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2012, 3, 8), 10d)
            .put(date(2012, 3, 9), 20d)
            .put(date(2012, 3, 10), 30d)
            .build();

    Map<IndexRateId, LocalDateDoubleTimeSeries> timeSeriesMap =
        ImmutableMap.of(
            LIBOR_1M_ID, libor1mTimeSeries,
            LIBOR_3M_ID, libor3mTimeSeries);

    DefaultMarketDataFactory factory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(timeSeriesMap),
            new DelegateBuilder(),
            Optional::of);

    MarketDataRequirements marketDataRequirements = MarketDataRequirements.builder().addTimeSeries(requirements).build();
    MarketDataResult result =
        factory.buildBaseMarketData(marketDataRequirements, BaseMarketData.empty(date(2011, 3, 8)));
    Map<MarketDataId<?>, Result<?>> failures = result.getTimeSeriesFailures();
    BaseMarketData marketData = result.getMarketData();

    assertThat(marketData.getTimeSeries(LIBOR_1M_ID)).isEqualTo(libor1mTimeSeries);
    assertThat(marketData.getTimeSeries(LIBOR_3M_ID)).isEqualTo(libor3mTimeSeries);
    assertThat(failures.get(libor6mId)).hasFailureMessageMatching("No market data rule.*");
    assertThat(failures.get(libor12mId)).hasFailureMessageMatching("No market data rule.*");
  }

  /**
   * Tests building market data that depends on other market data.
   */
  public void buildDataFromOtherData() {
    TestMarketDataBuilderB builderB = new TestMarketDataBuilderB();
    TestMarketDataBuilderC builderC = new TestMarketDataBuilderC();

    MarketDataRequirements requirements =
        MarketDataRequirements.builder()
            .addValues(new TestIdB("1"), new TestIdB("2"))
            .build();

    LocalDateDoubleTimeSeries timeSeries1 =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2011, 3, 8), 1)
            .put(date(2011, 3, 9), 2)
            .put(date(2011, 3, 10), 3)
            .build();

    LocalDateDoubleTimeSeries timeSeries2 =
        LocalDateDoubleTimeSeries.builder()
            .put(date(2011, 3, 8), 10)
            .put(date(2011, 3, 9), 20)
            .put(date(2011, 3, 10), 30)
            .build();

    Map<TestIdA, LocalDateDoubleTimeSeries> timeSeriesMap =
        ImmutableMap.of(
            new TestIdA("1"), timeSeries1,
            new TestIdA("2"), timeSeries2);

    TimeSeriesProvider timeSeriesProvider = new TestTimeSeriesProvider(timeSeriesMap);

    DefaultMarketDataFactory marketDataFactory =
        new DefaultMarketDataFactory(
            timeSeriesProvider,
            new TestObservableMarketDataBuilder(),
            FeedIdMapping.identity(),
            builderB,
            builderC);

    MarketDataResult result = marketDataFactory.buildBaseMarketData(
        requirements,
        BaseMarketData.empty(date(2011, 3, 8)));

    assertThat(result.getSingleValueFailures()).isEmpty();
    assertThat(result.getTimeSeriesFailures()).isEmpty();

    BaseMarketData marketData = result.getMarketData();
    TestMarketDataB marketDataB1 = marketData.getValue(new TestIdB("1"));
    TestMarketDataB marketDataB2 = marketData.getValue(new TestIdB("2"));

    TestMarketDataB expectedB1 = new TestMarketDataB(1, new TestMarketDataC(timeSeries1));
    TestMarketDataB expectedB2 = new TestMarketDataB(2, new TestMarketDataC(timeSeries2));

    assertThat(marketDataB1).isEqualTo(expectedB1);
    assertThat(marketDataB2).isEqualTo(expectedB2);
  }

  /**
   * Tests failures when there is no builder for an ID type.
   */
  public void noMarketDataBuilderAvailable() {
    TestIdB idB1 = new TestIdB("1");
    TestIdB idB2 = new TestIdB("2");
    TestIdC idC1 = new TestIdC("1");
    TestIdC idC2 = new TestIdC("2");
    TestMarketDataBuilderB builder = new TestMarketDataBuilderB();

    MarketDataRequirements requirements =
        MarketDataRequirements.builder()
            .addValues(idB1, idB2)
            .build();

    DefaultMarketDataFactory marketDataFactory =
        new DefaultMarketDataFactory(
            new TestTimeSeriesProvider(ImmutableMap.of()),
            new TestObservableMarketDataBuilder(),
            FeedIdMapping.identity(),
            builder);

    MarketDataResult result = marketDataFactory.buildBaseMarketData(
        requirements,
        BaseMarketData.empty(date(2011, 3, 8)));
    Map<MarketDataId<?>, Result<?>> failures = result.getSingleValueFailures();

    Result<?> failureB1 = failures.get(idB1);
    Result<?> failureB2 = failures.get(idB2);
    Result<?> failureC1 = failures.get(idC1);
    Result<?> failureC2 = failures.get(idC2);

    assertThat(failureB1).hasFailureMessageMatching("No value for.*");
    assertThat(failureB2).hasFailureMessageMatching("No value for.*");
    assertThat(failureC1).hasFailureMessageMatching("No market data builder available to handle.*");
    assertThat(failureC2).hasFailureMessageMatching("No market data builder available to handle.*");
  }

  //-----------------------------------------------------------------------------------------------------------

  private static final class DelegateBuilder implements ObservableMarketDataBuilder {

    private final Map<ObservableId, Result<Double>> marketData =
        ImmutableMap.of(
            LIBOR_1M_ID, Result.success(1d),
            LIBOR_3M_ID, Result.success(3d));

    @Override
    public Map<ObservableId, Result<Double>> build(Set<? extends ObservableId> requirements) {
      return requirements.stream()
          .filter(marketData::containsKey)
          .collect(toImmutableMap(id -> id, marketData::get));
    }
  }

  /**
   * Simple time series provider backed by a map.
   */
  private static final class TestTimeSeriesProvider implements TimeSeriesProvider {

    private final Map<? extends ObservableId, LocalDateDoubleTimeSeries> timeSeries;

    private TestTimeSeriesProvider(Map<? extends ObservableId, LocalDateDoubleTimeSeries> timeSeries) {
      this.timeSeries = timeSeries;
    }

    @Override
    public Result<LocalDateDoubleTimeSeries> timeSeries(ObservableId id) {
      LocalDateDoubleTimeSeries series = timeSeries.get(id);
      return Result.ofNullable(series, FailureReason.MISSING_DATA, "No time series found for ID {}", id);
    }
  }

  /**
   * Builds observable data by parsing the value of the standard ID.
   */
  private static final class TestObservableMarketDataBuilder implements ObservableMarketDataBuilder {

    @Override
    public Map<ObservableId, Result<Double>> build(Set<? extends ObservableId> requirements) {
      return requirements.stream().collect(toImmutableMap(id -> id, this::buildResult));
    }

    private Result<Double> buildResult(ObservableId id) {
      return Result.success(Double.parseDouble(id.getStandardId().getValue()));
    }
  }

  /**
   * Simple ID mapping backed by a map.
   */
  private static final class TestFeedIdMapping implements FeedIdMapping {

    private final Map<ObservableId, ObservableId> idMap =
        ImmutableMap.of(
            QuoteId.of(StandardId.of("reqs", "a")), QuoteId.of(StandardId.of("vendor", "1")),
            QuoteId.of(StandardId.of("reqs", "b")), QuoteId.of(StandardId.of("vendor", "2")));

    @Override
    public Optional<ObservableId> idForFeed(ObservableId id) {
      return Optional.ofNullable(idMap.get(id));
    }
  }

  private static final class CurveBuilder implements MarketDataBuilder<YieldCurve, DiscountingCurveId> {

    @Override
    public MarketDataRequirements requirements(DiscountingCurveId id) {
      return MarketDataRequirements.EMPTY;
    }

    @Override
    public Result<YieldCurve> build(
        DiscountingCurveId id,
        BaseMarketData builtData) {

      DiscountingCurveId curveId = DiscountingCurveId.of(Currency.EUR, "curve group");

      if (id.equals(curveId)) {
        YieldCurve yieldCurve = mock(YieldCurve.class);
        return Result.success(yieldCurve);
      } else {
        throw new IllegalArgumentException("Unexpected requirement " + id);
      }
    }

    @Override
    public Class<DiscountingCurveId> getMarketDataIdType() {
      return DiscountingCurveId.class;
    }
  }

  //-----------------------------------------------------------------------------------------------------------

  private static final class TestIdA implements ObservableId {

    private final StandardId id;

    TestIdA(String id) {
      this.id = StandardId.of("test", id);
    }

    @Override
    public StandardId getStandardId() {
      return id;
    }

    @Override
    public FieldName getFieldName() {
      return FieldName.MARKET_VALUE;
    }

    @Override
    public MarketDataFeed getMarketDataFeed() {
      return MarketDataFeed.NONE;
    }

    @Override
    public Class<Double> getMarketDataType() {
      return Double.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestIdA id = (TestIdA) o;
      return Objects.equals(this.id, id.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }

    @Override
    public String toString() {
      return "TestIdA [id=" + id + "]";
    }
  }

  private static final class TestIdB implements MarketDataId<TestMarketDataB> {

    private final String str;

    TestIdB(String str) {
      this.str = str;
    }

    @Override
    public Class<TestMarketDataB> getMarketDataType() {
      return TestMarketDataB.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestIdB id = (TestIdB) o;
      return Objects.equals(str, id.str);
    }

    @Override
    public int hashCode() {
      return Objects.hash(str);
    }

    @Override
    public String toString() {
      return "TestIdB [str='" + str + "']";
    }
  }

  private static final class TestIdC implements MarketDataId<TestMarketDataC> {

    private final String str;

    TestIdC(String str) {
      this.str = str;
    }

    @Override
    public Class<TestMarketDataC> getMarketDataType() {
      return TestMarketDataC.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestIdC id = (TestIdC) o;
      return Objects.equals(str, id.str);
    }

    @Override
    public int hashCode() {
      return Objects.hash(str);
    }

    @Override
    public String toString() {
      return "TestIdC [str='" + str + "']";
    }
  }

  private static final class TestMarketDataB {

    private final double value;

    private final TestMarketDataC marketData;

    TestMarketDataB(double value, TestMarketDataC marketData) {
      this.value = value;
      this.marketData = marketData;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestMarketDataB that = (TestMarketDataB) o;
      return Objects.equals(value, that.value) &&
          Objects.equals(marketData, that.marketData);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value, marketData);
    }
  }

  private final class TestMarketDataBuilderB implements MarketDataBuilder<TestMarketDataB, TestIdB> {

    @Override
    public MarketDataRequirements requirements(TestIdB id) {
      return MarketDataRequirements.builder()
          .addValues(new TestIdA(id.str), new TestIdC(id.str))
          .build();
    }

    @Override
    public Result<TestMarketDataB> build(TestIdB id, BaseMarketData builtData) {
      TestIdA idA = new TestIdA(id.str);
      TestIdC idC = new TestIdC(id.str);

      if (!builtData.containsValue(idA)) {
        return Result.failure(FailureReason.MISSING_DATA, "No value for {}", idA);
      }
      if (!builtData.containsValue(idC)) {
        return Result.failure(FailureReason.MISSING_DATA, "No value for {}", idC);
      }
      Double value = builtData.getValue(idA);
      TestMarketDataC marketDataC = builtData.getValue(idC);
      return Result.success(new TestMarketDataB(value, marketDataC));
    }

    @Override
    public Class<TestIdB> getMarketDataIdType() {
      return TestIdB.class;
    }
  }

  private static final class TestMarketDataC {

    private final LocalDateDoubleTimeSeries timeSeries;

    TestMarketDataC(LocalDateDoubleTimeSeries timeSeries) {
      this.timeSeries = timeSeries;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestMarketDataC that = (TestMarketDataC) o;
      return Objects.equals(timeSeries, that.timeSeries);
    }

    @Override
    public int hashCode() {
      return Objects.hash(timeSeries);
    }
  }

  private static final class TestMarketDataBuilderC implements MarketDataBuilder<TestMarketDataC, TestIdC> {

    @Override
    public MarketDataRequirements requirements(TestIdC id) {
      return MarketDataRequirements.builder()
          .addTimeSeries(new TestIdA(id.str))
          .build();
    }

    @Override
    public Result<TestMarketDataC> build(TestIdC id, BaseMarketData builtData) {
      LocalDateDoubleTimeSeries timeSeries = builtData.getTimeSeries(new TestIdA(id.str));
      return Result.success(new TestMarketDataC(timeSeries));
    }

    @Override
    public Class<TestIdC> getMarketDataIdType() {
      return TestIdC.class;
    }
  }
}