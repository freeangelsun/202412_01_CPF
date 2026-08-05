package org.springframework.jms.core; @FunctionalInterface public interface MessageCreator { jakarta.jms.Message createMessage(jakarta.jms.Session session); }
