package org.springframework.jms.core; public abstract class JmsTemplate { public abstract void send(String destination, MessageCreator creator); }
