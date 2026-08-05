package org.apache.kafka.clients.producer;
import java.util.*; import org.apache.kafka.common.header.internals.RecordHeader;
public class ProducerRecord<K,V>{ private final String topic; private final K key; private final V value; private final Headers headers=new Headers();
 public ProducerRecord(String topic,K key,V value){this.topic=topic;this.key=key;this.value=value;} public String topic(){return topic;} public K key(){return key;} public V value(){return value;} public Headers headers(){return headers;}
 public static class Headers{private final java.util.List<RecordHeader> values=new ArrayList<>(); public Headers add(RecordHeader h){values.add(h);return this;} public RecordHeader lastHeader(String n){RecordHeader r=null;for(RecordHeader h:values)if(h.key().equals(n))r=h;return r;}}
}
