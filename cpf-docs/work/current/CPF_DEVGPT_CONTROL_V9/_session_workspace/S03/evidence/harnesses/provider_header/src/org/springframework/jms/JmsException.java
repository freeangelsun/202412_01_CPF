package org.springframework.jms; public class JmsException extends RuntimeException { public JmsException(String m){super(m);} public JmsException(String m,Throwable t){super(m,t);} }
