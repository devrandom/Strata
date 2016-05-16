/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.id;

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

import com.opengamma.strata.basics.market.FieldName;
import com.opengamma.strata.basics.market.MarketDataFeed;
import com.opengamma.strata.basics.market.ObservableId;
import com.opengamma.strata.basics.market.StandardId;

/**
 * The ID of a market quote.
 * <p>
 * A quote ID identifies a piece of data in an external data provider.
 * <p>
 * Where possible, applications should use higher level IDs, instead of this class.
 * Higher level market data keys allow the system to associate the market data with metadata when
 * applying scenario definitions. If quote IDs are used directly, the system has no way to
 * perturb the market data using higher level rules that rely on metadata.
 * <p>
 * The {@link StandardId} in a quote ID is typically the ID from an underlying data provider (e.g.
 * Bloomberg or Reuters). However the field name is a generic name which is mapped to the field name
 * in the underlying provider by the market data system.
 * <p>
 * The reason for this difference is the different sources of the ID and field name data. The ID is typically
 * taken from an object which is provided to any calculations, for example a security linked to the
 * trade. The calculation rarely has to make up an ID for an object it doesn't have access to.
 * <p>
 * In contrast, calculations will often have to reference field names that aren't part their arguments. For
 * example, if a calculation requires the last closing price of a security, it could take the ID from
 * the security, but it needs a way to specify the field name containing the last closing price.
 * <p>
 * If the field name were specific to the market data provider, the calculation would have to be aware
 * of the source of its market data. However, if it uses a generic field name from {@code FieldNames}
 * the market data source can change without affecting the calculation.
 *
 * @see FieldName
 */
@BeanDefinition(builderScope = "private")
public final class QuoteId implements ObservableId, ImmutableBean, Serializable {

  /** The ID of the data, typically an ID from an external data provider. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final StandardId standardId;

  /** The field name in the market data record that contains the data. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final FieldName fieldName;

  /** The market data feed from which the market data should be retrieved. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final MarketDataFeed marketDataFeed;

  /**
   * Returns an ID representing a market quote with a field name of {@link FieldName#MARKET_VALUE}
   * and a market data feed of {@link MarketDataFeed#NONE}.
   *
   * @param id  the ID of the data in the underlying data provider
   * @return an ID representing a market quote
   */
  public static QuoteId of(StandardId id) {
    return new QuoteId(id, FieldName.MARKET_VALUE, MarketDataFeed.NONE);
  }

  /**
   * Returns an ID representing a market quote with a field name of {@link FieldName#MARKET_VALUE}.
   *
   * @param id  the ID of the data in the underlying data provider
   * @param feed  the market data feed from which the market data should be retrieved
   * @return an ID representing a market quote
   */
  public static QuoteId of(StandardId id, MarketDataFeed feed) {
    return new QuoteId(id, FieldName.MARKET_VALUE, feed);
  }

  /**
   * Returns an ID representing a market quote with a field name
   * and a market data feed of {@link MarketDataFeed#NONE}.
   *
   * @param id  the ID of the data in the underlying data provider
   * @param fieldName  the name of the field in the market data record holding the data
   * @return an ID representing a market quote
   */
  public static QuoteId of(StandardId id, FieldName fieldName) {
    return new QuoteId(id, fieldName, MarketDataFeed.NONE);
  }

  /**
   * Returns an ID representing a market quote.
   *
   * @param id  the ID of the data in the underlying data provider
   * @param feed  the market data feed from which the market data should be retrieved
   * @param fieldName  the name of the field in the market data record holding the data
   * @return an ID representing a market quote
   */
  public static QuoteId of(StandardId id, MarketDataFeed feed, FieldName fieldName) {
    return new QuoteId(id, fieldName, feed);
  }

  //-------------------------------------------------------------------------
  @Override
  public QuoteId withMarketDataFeed(MarketDataFeed feed) {
    return new QuoteId(standardId, fieldName, feed);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code QuoteId}.
   * @return the meta-bean, not null
   */
  public static QuoteId.Meta meta() {
    return QuoteId.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(QuoteId.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private QuoteId(
      StandardId standardId,
      FieldName fieldName,
      MarketDataFeed marketDataFeed) {
    JodaBeanUtils.notNull(standardId, "standardId");
    JodaBeanUtils.notNull(fieldName, "fieldName");
    JodaBeanUtils.notNull(marketDataFeed, "marketDataFeed");
    this.standardId = standardId;
    this.fieldName = fieldName;
    this.marketDataFeed = marketDataFeed;
  }

  @Override
  public QuoteId.Meta metaBean() {
    return QuoteId.Meta.INSTANCE;
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
   * Gets the ID of the data, typically an ID from an external data provider.
   * @return the value of the property, not null
   */
  @Override
  public StandardId getStandardId() {
    return standardId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the field name in the market data record that contains the data.
   * @return the value of the property, not null
   */
  @Override
  public FieldName getFieldName() {
    return fieldName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market data feed from which the market data should be retrieved.
   * @return the value of the property, not null
   */
  @Override
  public MarketDataFeed getMarketDataFeed() {
    return marketDataFeed;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      QuoteId other = (QuoteId) obj;
      return JodaBeanUtils.equal(standardId, other.standardId) &&
          JodaBeanUtils.equal(fieldName, other.fieldName) &&
          JodaBeanUtils.equal(marketDataFeed, other.marketDataFeed);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(standardId);
    hash = hash * 31 + JodaBeanUtils.hashCode(fieldName);
    hash = hash * 31 + JodaBeanUtils.hashCode(marketDataFeed);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("QuoteId{");
    buf.append("standardId").append('=').append(standardId).append(',').append(' ');
    buf.append("fieldName").append('=').append(fieldName).append(',').append(' ');
    buf.append("marketDataFeed").append('=').append(JodaBeanUtils.toString(marketDataFeed));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code QuoteId}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code standardId} property.
     */
    private final MetaProperty<StandardId> standardId = DirectMetaProperty.ofImmutable(
        this, "standardId", QuoteId.class, StandardId.class);
    /**
     * The meta-property for the {@code fieldName} property.
     */
    private final MetaProperty<FieldName> fieldName = DirectMetaProperty.ofImmutable(
        this, "fieldName", QuoteId.class, FieldName.class);
    /**
     * The meta-property for the {@code marketDataFeed} property.
     */
    private final MetaProperty<MarketDataFeed> marketDataFeed = DirectMetaProperty.ofImmutable(
        this, "marketDataFeed", QuoteId.class, MarketDataFeed.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "standardId",
        "fieldName",
        "marketDataFeed");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return standardId;
        case 1265009317:  // fieldName
          return fieldName;
        case 842621124:  // marketDataFeed
          return marketDataFeed;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends QuoteId> builder() {
      return new QuoteId.Builder();
    }

    @Override
    public Class<? extends QuoteId> beanType() {
      return QuoteId.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code standardId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<StandardId> standardId() {
      return standardId;
    }

    /**
     * The meta-property for the {@code fieldName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FieldName> fieldName() {
      return fieldName;
    }

    /**
     * The meta-property for the {@code marketDataFeed} property.
     * @return the meta-property, not null
     */
    public MetaProperty<MarketDataFeed> marketDataFeed() {
      return marketDataFeed;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return ((QuoteId) bean).getStandardId();
        case 1265009317:  // fieldName
          return ((QuoteId) bean).getFieldName();
        case 842621124:  // marketDataFeed
          return ((QuoteId) bean).getMarketDataFeed();
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
   * The bean-builder for {@code QuoteId}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<QuoteId> {

    private StandardId standardId;
    private FieldName fieldName;
    private MarketDataFeed marketDataFeed;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          return standardId;
        case 1265009317:  // fieldName
          return fieldName;
        case 842621124:  // marketDataFeed
          return marketDataFeed;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1284477768:  // standardId
          this.standardId = (StandardId) newValue;
          break;
        case 1265009317:  // fieldName
          this.fieldName = (FieldName) newValue;
          break;
        case 842621124:  // marketDataFeed
          this.marketDataFeed = (MarketDataFeed) newValue;
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
    public QuoteId build() {
      return new QuoteId(
          standardId,
          fieldName,
          marketDataFeed);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("QuoteId.Builder{");
      buf.append("standardId").append('=').append(JodaBeanUtils.toString(standardId)).append(',').append(' ');
      buf.append("fieldName").append('=').append(JodaBeanUtils.toString(fieldName)).append(',').append(' ');
      buf.append("marketDataFeed").append('=').append(JodaBeanUtils.toString(marketDataFeed));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
