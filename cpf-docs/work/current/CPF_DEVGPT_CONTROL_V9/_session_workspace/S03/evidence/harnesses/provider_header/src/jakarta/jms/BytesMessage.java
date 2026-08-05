package jakarta.jms; public interface BytesMessage extends Message { void writeBytes(byte[] b); void setJMSCorrelationID(String id); void setStringProperty(String n,String v); }
