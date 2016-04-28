/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.tree;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.PutCall;
import com.opengamma.strata.collect.array.DoubleArray;

/**
 * European vanilla option function. 
 */
@BeanDefinition(builderScope = "private")
public final class EuropeanVanillaOptionFunction
    implements OptionFunction, ImmutableBean, Serializable {

  /**
   * The strike value.
   */
  @PropertyDefinition(overrideGet = true)
  private final double strike;
  /**
   * The time to expiry.
   */
  @PropertyDefinition(overrideGet = true)
  private final double timeToExpiry;
  /**
   * The sign. 
   * <p>
   * The sign is +1 for call and -1 for put.
   */
  @PropertyDefinition
  private final double sign;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance. 
   * 
   * @param strike  the strike
   * @param timeToExpiry  the time to expiry
   * @param putCall  put or call
   * @return the instance
   */
  public static EuropeanVanillaOptionFunction of(double strike, double timeToExpiry, PutCall putCall) {
    double sign = putCall.isCall() ? 1d : -1d;
    return new EuropeanVanillaOptionFunction(strike, timeToExpiry, sign);
  }

  //-------------------------------------------------------------------------
  //  @Override
  //  public DoubleArray getPayoffAtExpiryTrinomial(double spot, double downFactor, double middleOverDown) {
  //    int nNodes = 2 * numberOfSteps + 1;
  //    double[] values = new double[nNodes];
  //    double priceTmp = spot * Math.pow(downFactor, numberOfSteps);
  //    for (int i = 0; i < nNodes; ++i) {
  //      values[i] = Math.max(sign * (priceTmp - strike), 0d);
  //      priceTmp *= middleOverDown;
  //    }
  //    return DoubleArray.ofUnsafe(values);
  //  }

  @Override
  public DoubleArray getPayoffAtExpiryTrinomial(DoubleArray stateValue) {
    int nNodes = stateValue.size();
    double[] values = new double[nNodes];
    for (int i = 0; i < nNodes; ++i) {
      values[i] = Math.max(sign * (stateValue.get(i) - strike), 0d);
    }
    return DoubleArray.ofUnsafe(values);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code EuropeanVanillaOptionFunction}.
   * @return the meta-bean, not null
   */
  public static EuropeanVanillaOptionFunction.Meta meta() {
    return EuropeanVanillaOptionFunction.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(EuropeanVanillaOptionFunction.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private EuropeanVanillaOptionFunction(
      double strike,
      double timeToExpiry,
      double sign) {
    this.strike = strike;
    this.timeToExpiry = timeToExpiry;
    this.sign = sign;
  }

  @Override
  public EuropeanVanillaOptionFunction.Meta metaBean() {
    return EuropeanVanillaOptionFunction.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the strike value.
   * @return the value of the property
   */
  @Override
  public double getStrike() {
    return strike;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time to expiry.
   * @return the value of the property
   */
  @Override
  public double getTimeToExpiry() {
    return timeToExpiry;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sign.
   * <p>
   * The sign is +1 for call and -1 for put.
   * @return the value of the property
   */
  public double getSign() {
    return sign;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      EuropeanVanillaOptionFunction other = (EuropeanVanillaOptionFunction) obj;
      return JodaBeanUtils.equal(strike, other.strike) &&
          JodaBeanUtils.equal(timeToExpiry, other.timeToExpiry) &&
          JodaBeanUtils.equal(sign, other.sign);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(strike);
    hash = hash * 31 + JodaBeanUtils.hashCode(timeToExpiry);
    hash = hash * 31 + JodaBeanUtils.hashCode(sign);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("EuropeanVanillaOptionFunction{");
    buf.append("strike").append('=').append(strike).append(',').append(' ');
    buf.append("timeToExpiry").append('=').append(timeToExpiry).append(',').append(' ');
    buf.append("sign").append('=').append(JodaBeanUtils.toString(sign));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code EuropeanVanillaOptionFunction}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code strike} property.
     */
    private final MetaProperty<Double> strike = DirectMetaProperty.ofImmutable(
        this, "strike", EuropeanVanillaOptionFunction.class, Double.TYPE);
    /**
     * The meta-property for the {@code timeToExpiry} property.
     */
    private final MetaProperty<Double> timeToExpiry = DirectMetaProperty.ofImmutable(
        this, "timeToExpiry", EuropeanVanillaOptionFunction.class, Double.TYPE);
    /**
     * The meta-property for the {@code sign} property.
     */
    private final MetaProperty<Double> sign = DirectMetaProperty.ofImmutable(
        this, "sign", EuropeanVanillaOptionFunction.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "strike",
        "timeToExpiry",
        "sign");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -891985998:  // strike
          return strike;
        case -1831499397:  // timeToExpiry
          return timeToExpiry;
        case 3530173:  // sign
          return sign;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends EuropeanVanillaOptionFunction> builder() {
      return new EuropeanVanillaOptionFunction.Builder();
    }

    @Override
    public Class<? extends EuropeanVanillaOptionFunction> beanType() {
      return EuropeanVanillaOptionFunction.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code strike} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> strike() {
      return strike;
    }

    /**
     * The meta-property for the {@code timeToExpiry} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> timeToExpiry() {
      return timeToExpiry;
    }

    /**
     * The meta-property for the {@code sign} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> sign() {
      return sign;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -891985998:  // strike
          return ((EuropeanVanillaOptionFunction) bean).getStrike();
        case -1831499397:  // timeToExpiry
          return ((EuropeanVanillaOptionFunction) bean).getTimeToExpiry();
        case 3530173:  // sign
          return ((EuropeanVanillaOptionFunction) bean).getSign();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code EuropeanVanillaOptionFunction}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<EuropeanVanillaOptionFunction> {

    private double strike;
    private double timeToExpiry;
    private double sign;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -891985998:  // strike
          return strike;
        case -1831499397:  // timeToExpiry
          return timeToExpiry;
        case 3530173:  // sign
          return sign;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -891985998:  // strike
          this.strike = (Double) newValue;
          break;
        case -1831499397:  // timeToExpiry
          this.timeToExpiry = (Double) newValue;
          break;
        case 3530173:  // sign
          this.sign = (Double) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public EuropeanVanillaOptionFunction build() {
      return new EuropeanVanillaOptionFunction(
          strike,
          timeToExpiry,
          sign);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("EuropeanVanillaOptionFunction.Builder{");
      buf.append("strike").append('=').append(JodaBeanUtils.toString(strike)).append(',').append(' ');
      buf.append("timeToExpiry").append('=').append(JodaBeanUtils.toString(timeToExpiry)).append(',').append(' ');
      buf.append("sign").append('=').append(JodaBeanUtils.toString(sign));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}