/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.zhigalko.core.schema;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class UpdateCustomerAddressAvroEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 8636007465896325063L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"UpdateCustomerAddressAvroEvent\",\"namespace\":\"com.zhigalko.core.schema\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"address\",\"type\":\"string\"},{\"name\":\"timestamp\",\"type\":\"string\"},{\"name\":\"eventType\",\"type\":\"string\"},{\"name\":\"aggregateId\",\"type\":\"long\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<UpdateCustomerAddressAvroEvent> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<UpdateCustomerAddressAvroEvent> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<UpdateCustomerAddressAvroEvent> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<UpdateCustomerAddressAvroEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<UpdateCustomerAddressAvroEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this UpdateCustomerAddressAvroEvent to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a UpdateCustomerAddressAvroEvent from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a UpdateCustomerAddressAvroEvent instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static UpdateCustomerAddressAvroEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.CharSequence id;
  private java.lang.CharSequence address;
  private java.lang.CharSequence timestamp;
  private java.lang.CharSequence eventType;
  private long aggregateId;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public UpdateCustomerAddressAvroEvent() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param address The new value for address
   * @param timestamp The new value for timestamp
   * @param eventType The new value for eventType
   * @param aggregateId The new value for aggregateId
   */
  public UpdateCustomerAddressAvroEvent(java.lang.CharSequence id, java.lang.CharSequence address, java.lang.CharSequence timestamp, java.lang.CharSequence eventType, java.lang.Long aggregateId) {
    this.id = id;
    this.address = address;
    this.timestamp = timestamp;
    this.eventType = eventType;
    this.aggregateId = aggregateId;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return address;
    case 2: return timestamp;
    case 3: return eventType;
    case 4: return aggregateId;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: address = (java.lang.CharSequence)value$; break;
    case 2: timestamp = (java.lang.CharSequence)value$; break;
    case 3: eventType = (java.lang.CharSequence)value$; break;
    case 4: aggregateId = (java.lang.Long)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.CharSequence getId() {
    return id;
  }


  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'address' field.
   * @return The value of the 'address' field.
   */
  public java.lang.CharSequence getAddress() {
    return address;
  }


  /**
   * Sets the value of the 'address' field.
   * @param value the value to set.
   */
  public void setAddress(java.lang.CharSequence value) {
    this.address = value;
  }

  /**
   * Gets the value of the 'timestamp' field.
   * @return The value of the 'timestamp' field.
   */
  public java.lang.CharSequence getTimestamp() {
    return timestamp;
  }


  /**
   * Sets the value of the 'timestamp' field.
   * @param value the value to set.
   */
  public void setTimestamp(java.lang.CharSequence value) {
    this.timestamp = value;
  }

  /**
   * Gets the value of the 'eventType' field.
   * @return The value of the 'eventType' field.
   */
  public java.lang.CharSequence getEventType() {
    return eventType;
  }


  /**
   * Sets the value of the 'eventType' field.
   * @param value the value to set.
   */
  public void setEventType(java.lang.CharSequence value) {
    this.eventType = value;
  }

  /**
   * Gets the value of the 'aggregateId' field.
   * @return The value of the 'aggregateId' field.
   */
  public long getAggregateId() {
    return aggregateId;
  }


  /**
   * Sets the value of the 'aggregateId' field.
   * @param value the value to set.
   */
  public void setAggregateId(long value) {
    this.aggregateId = value;
  }

  /**
   * Creates a new UpdateCustomerAddressAvroEvent RecordBuilder.
   * @return A new UpdateCustomerAddressAvroEvent RecordBuilder
   */
  public static com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder newBuilder() {
    return new com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder();
  }

  /**
   * Creates a new UpdateCustomerAddressAvroEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new UpdateCustomerAddressAvroEvent RecordBuilder
   */
  public static com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder newBuilder(com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder other) {
    if (other == null) {
      return new com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder();
    } else {
      return new com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder(other);
    }
  }

  /**
   * Creates a new UpdateCustomerAddressAvroEvent RecordBuilder by copying an existing UpdateCustomerAddressAvroEvent instance.
   * @param other The existing instance to copy.
   * @return A new UpdateCustomerAddressAvroEvent RecordBuilder
   */
  public static com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder newBuilder(com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent other) {
    if (other == null) {
      return new com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder();
    } else {
      return new com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder(other);
    }
  }

  /**
   * RecordBuilder for UpdateCustomerAddressAvroEvent instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<UpdateCustomerAddressAvroEvent>
    implements org.apache.avro.data.RecordBuilder<UpdateCustomerAddressAvroEvent> {

    private java.lang.CharSequence id;
    private java.lang.CharSequence address;
    private java.lang.CharSequence timestamp;
    private java.lang.CharSequence eventType;
    private long aggregateId;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.address)) {
        this.address = data().deepCopy(fields()[1].schema(), other.address);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[2].schema(), other.timestamp);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.eventType)) {
        this.eventType = data().deepCopy(fields()[3].schema(), other.eventType);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.aggregateId)) {
        this.aggregateId = data().deepCopy(fields()[4].schema(), other.aggregateId);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
    }

    /**
     * Creates a Builder by copying an existing UpdateCustomerAddressAvroEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.address)) {
        this.address = data().deepCopy(fields()[1].schema(), other.address);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[2].schema(), other.timestamp);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.eventType)) {
        this.eventType = data().deepCopy(fields()[3].schema(), other.eventType);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.aggregateId)) {
        this.aggregateId = data().deepCopy(fields()[4].schema(), other.aggregateId);
        fieldSetFlags()[4] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.CharSequence getId() {
      return id;
    }


    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder setId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'address' field.
      * @return The value.
      */
    public java.lang.CharSequence getAddress() {
      return address;
    }


    /**
      * Sets the value of the 'address' field.
      * @param value The value of 'address'.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder setAddress(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.address = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'address' field has been set.
      * @return True if the 'address' field has been set, false otherwise.
      */
    public boolean hasAddress() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'address' field.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder clearAddress() {
      address = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'timestamp' field.
      * @return The value.
      */
    public java.lang.CharSequence getTimestamp() {
      return timestamp;
    }


    /**
      * Sets the value of the 'timestamp' field.
      * @param value The value of 'timestamp'.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder setTimestamp(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.timestamp = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'timestamp' field has been set.
      * @return True if the 'timestamp' field has been set, false otherwise.
      */
    public boolean hasTimestamp() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'timestamp' field.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder clearTimestamp() {
      timestamp = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'eventType' field.
      * @return The value.
      */
    public java.lang.CharSequence getEventType() {
      return eventType;
    }


    /**
      * Sets the value of the 'eventType' field.
      * @param value The value of 'eventType'.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder setEventType(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.eventType = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'eventType' field has been set.
      * @return True if the 'eventType' field has been set, false otherwise.
      */
    public boolean hasEventType() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'eventType' field.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder clearEventType() {
      eventType = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'aggregateId' field.
      * @return The value.
      */
    public long getAggregateId() {
      return aggregateId;
    }


    /**
      * Sets the value of the 'aggregateId' field.
      * @param value The value of 'aggregateId'.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder setAggregateId(long value) {
      validate(fields()[4], value);
      this.aggregateId = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'aggregateId' field has been set.
      * @return True if the 'aggregateId' field has been set, false otherwise.
      */
    public boolean hasAggregateId() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'aggregateId' field.
      * @return This builder.
      */
    public com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent.Builder clearAggregateId() {
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UpdateCustomerAddressAvroEvent build() {
      try {
        UpdateCustomerAddressAvroEvent record = new UpdateCustomerAddressAvroEvent();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.address = fieldSetFlags()[1] ? this.address : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.timestamp = fieldSetFlags()[2] ? this.timestamp : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.eventType = fieldSetFlags()[3] ? this.eventType : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.aggregateId = fieldSetFlags()[4] ? this.aggregateId : (java.lang.Long) defaultValue(fields()[4]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<UpdateCustomerAddressAvroEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<UpdateCustomerAddressAvroEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<UpdateCustomerAddressAvroEvent>
    READER$ = (org.apache.avro.io.DatumReader<UpdateCustomerAddressAvroEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.id);

    out.writeString(this.address);

    out.writeString(this.timestamp);

    out.writeString(this.eventType);

    out.writeLong(this.aggregateId);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.id = in.readString(this.id instanceof Utf8 ? (Utf8)this.id : null);

      this.address = in.readString(this.address instanceof Utf8 ? (Utf8)this.address : null);

      this.timestamp = in.readString(this.timestamp instanceof Utf8 ? (Utf8)this.timestamp : null);

      this.eventType = in.readString(this.eventType instanceof Utf8 ? (Utf8)this.eventType : null);

      this.aggregateId = in.readLong();

    } else {
      for (int i = 0; i < 5; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.id = in.readString(this.id instanceof Utf8 ? (Utf8)this.id : null);
          break;

        case 1:
          this.address = in.readString(this.address instanceof Utf8 ? (Utf8)this.address : null);
          break;

        case 2:
          this.timestamp = in.readString(this.timestamp instanceof Utf8 ? (Utf8)this.timestamp : null);
          break;

        case 3:
          this.eventType = in.readString(this.eventType instanceof Utf8 ? (Utf8)this.eventType : null);
          break;

        case 4:
          this.aggregateId = in.readLong();
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










