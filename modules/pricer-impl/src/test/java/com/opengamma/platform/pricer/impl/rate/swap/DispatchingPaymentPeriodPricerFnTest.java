/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.platform.pricer.impl.rate.swap;

import static com.opengamma.strata.collect.TestHelper.assertThrowsIllegalArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import com.opengamma.platform.pricer.PricingEnvironment;
import com.opengamma.platform.pricer.impl.MockPricingEnvironment;
import com.opengamma.platform.pricer.rate.swap.PaymentPeriodPricerFn;
import com.opengamma.strata.finance.rate.swap.PaymentPeriod;
import com.opengamma.strata.finance.rate.swap.RatePaymentPeriod;

/**
 * Test.
 */
@Test
public class DispatchingPaymentPeriodPricerFnTest {

  private static final PricingEnvironment MOCK_ENV = new MockPricingEnvironment();

  public void test_presentValue_RatePaymentPeriod() {
    double expected = 0.0123d;
    PaymentPeriodPricerFn<RatePaymentPeriod> mockNotionalExchangeFn = mock(PaymentPeriodPricerFn.class);
    when(mockNotionalExchangeFn.presentValue(MOCK_ENV, SwapDummyData.FIXED_RATE_PAYMENT_PERIOD))
        .thenReturn(expected);
    DispatchingPaymentPeriodPricerFn test = new DispatchingPaymentPeriodPricerFn(mockNotionalExchangeFn);
    assertEquals(test.presentValue(MOCK_ENV, SwapDummyData.FIXED_RATE_PAYMENT_PERIOD), expected, 0d);
  }

  public void test_presentValue_unknownType() {
    PaymentPeriod mockPaymentPeriod = mock(PaymentPeriod.class);
    DispatchingPaymentPeriodPricerFn test = DispatchingPaymentPeriodPricerFn.DEFAULT;
    assertThrowsIllegalArg(() -> test.presentValue(MOCK_ENV, mockPaymentPeriod));
  }

  //-------------------------------------------------------------------------
  public void test_futureValue_RatePaymentPeriod() {
    double expected = 0.0123d;
    PaymentPeriodPricerFn<RatePaymentPeriod> mockNotionalExchangeFn = mock(PaymentPeriodPricerFn.class);
    when(mockNotionalExchangeFn.futureValue(MOCK_ENV, SwapDummyData.FIXED_RATE_PAYMENT_PERIOD))
        .thenReturn(expected);
    DispatchingPaymentPeriodPricerFn test = new DispatchingPaymentPeriodPricerFn(mockNotionalExchangeFn);
    assertEquals(test.futureValue(MOCK_ENV, SwapDummyData.FIXED_RATE_PAYMENT_PERIOD), expected, 0d);
  }

  public void test_futureValue_unknownType() {
    PaymentPeriod mockPaymentPeriod = mock(PaymentPeriod.class);
    DispatchingPaymentPeriodPricerFn test = DispatchingPaymentPeriodPricerFn.DEFAULT;
    assertThrowsIllegalArg(() -> test.futureValue(MOCK_ENV, mockPaymentPeriod));
  }

}