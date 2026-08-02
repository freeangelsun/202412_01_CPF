package com.cpf.starter.notification;public interface CpfNotificationProvider {String channel();CpfNotificationResult send(CpfNotificationRequest request);}
