package org.springframework.core.env; public interface Environment {String getProperty(String k,String d);String[] getActiveProfiles();}
