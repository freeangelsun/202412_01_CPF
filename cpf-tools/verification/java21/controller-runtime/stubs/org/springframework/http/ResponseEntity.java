package org.springframework.http;
public final class ResponseEntity<T>{
  private final int status; private final T body;
  private ResponseEntity(int status,T body){this.status=status;this.body=body;}
  public static <T> ResponseEntity<T> ok(T body){return new ResponseEntity<>(200,body);}
  public static BodyBuilder status(int status){return new BodyBuilder(status);}
  public static BodyBuilder badRequest(){return new BodyBuilder(400);}
  public static BodyBuilder accepted(){return new BodyBuilder(202);}
  public int getStatusCodeValue(){return status;} public Status getStatusCode(){return new Status(status);} public T getBody(){return body;}
  public record Status(int value){}
  public static final class BodyBuilder{private final int status;BodyBuilder(int s){status=s;}public <T> ResponseEntity<T> body(T body){return new ResponseEntity<>(status,body);}}
}
