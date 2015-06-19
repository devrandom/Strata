/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.value;

import static com.opengamma.strata.basics.currency.Currency.GBP;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.date.DayCounts.ACT_365F;
import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static com.opengamma.strata.collect.TestHelper.date;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.strata.basics.interpolator.CurveInterpolator;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.DefaultCurveMetadata;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.sensitivity.CurveUnitParameterSensitivities;
import com.opengamma.strata.market.sensitivity.CurveUnitParameterSensitivity;
import com.opengamma.strata.market.sensitivity.ZeroRateSensitivity;

/**
 * Test {@link SimpleDiscountFactors}.
 */
@Test
public class SimpleDiscountFactorsTest {

  private static final LocalDate DATE_VAL = date(2015, 6, 4);
  private static final LocalDate DATE_AFTER = date(2015, 7, 30);

  private static final CurveInterpolator INTERPOLATOR = Interpolator1DFactory.LINEAR_INSTANCE;
  private static final CurveName NAME = CurveName.of("TestCurve");
  private static final CurveMetadata METADATA = DefaultCurveMetadata.of(NAME, ACT_365F);
  private static final InterpolatedNodalCurve CURVE =
      InterpolatedNodalCurve.of(METADATA, new double[] {0, 10}, new double[] {1, 2}, INTERPOLATOR);
  private static final InterpolatedNodalCurve CURVE2 =
      InterpolatedNodalCurve.of(METADATA, new double[] {0, 10}, new double[] {2, 3}, INTERPOLATOR);
  private static final InterpolatedNodalCurve CURVE_BAD =
      InterpolatedNodalCurve.of(NAME, new double[] {0, 10}, new double[] {1, 2}, INTERPOLATOR);

  //-------------------------------------------------------------------------
  public void test_of() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    assertEquals(test.getCurrency(), GBP);
    assertEquals(test.getValuationDate(), DATE_VAL);
    assertEquals(test.getCurve(), CURVE);
    assertEquals(test.getCurveName(), NAME);
    assertEquals(test.getParameterCount(), 2);
  }

  public void test_of_curveNoDayCount() {
    assertThrowsIllegalArg(() -> SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE_BAD));
  }

  //-------------------------------------------------------------------------
  public void test_discountFactor() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    double relativeYearFraction = ACT_365F.relativeYearFraction(DATE_VAL, DATE_AFTER);
    double expected = CURVE.yValue(relativeYearFraction);
    assertEquals(test.discountFactor(DATE_AFTER), expected);
  }

  //-------------------------------------------------------------------------
  public void test_zeroRatePointSensitivity() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    double relativeYearFraction = ACT_365F.relativeYearFraction(DATE_VAL, DATE_AFTER);
    double df = CURVE.yValue(relativeYearFraction);
    ZeroRateSensitivity expected = ZeroRateSensitivity.of(GBP, DATE_AFTER, -df * relativeYearFraction);
    assertEquals(test.zeroRatePointSensitivity(DATE_AFTER), expected);
  }

  public void test_zeroRatePointSensitivity_sensitivityCurrency() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    double relativeYearFraction = ACT_365F.relativeYearFraction(DATE_VAL, DATE_AFTER);
    double df = CURVE.yValue(relativeYearFraction);
    ZeroRateSensitivity expected = ZeroRateSensitivity.of(GBP, USD, DATE_AFTER, -df * relativeYearFraction);
    assertEquals(test.zeroRatePointSensitivity(DATE_AFTER, USD), expected);
  }

  //-------------------------------------------------------------------------
  public void test_unitParameterSensitivity() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    double relativeYearFraction = ACT_365F.relativeYearFraction(DATE_VAL, DATE_AFTER);
    CurveUnitParameterSensitivities expected = CurveUnitParameterSensitivities.of(
        CurveUnitParameterSensitivity.of(METADATA, CURVE.yValueParameterSensitivity(relativeYearFraction)));
    assertEquals(test.unitParameterSensitivity(DATE_AFTER), expected);
  }

  //-------------------------------------------------------------------------
  // proper end-to-end FD tests are elsewhere
  public void test_curveParameterSensitivity() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    ZeroRateSensitivity point = ZeroRateSensitivity.of(GBP, DATE_AFTER, 1d);
    assertEquals(test.curveParameterSensitivity(point).size(), 1);
  }

  //-------------------------------------------------------------------------
  public void test_withCurve() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE).withCurve(CURVE2);
    assertEquals(test.getCurve(), CURVE2);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    SimpleDiscountFactors test = SimpleDiscountFactors.of(GBP, DATE_VAL, CURVE);
    coverImmutableBean(test);
    SimpleDiscountFactors test2 = SimpleDiscountFactors.of(USD, DATE_VAL.plusDays(1), CURVE2);
    coverBeanEquals(test, test2);
  }

}