package com.cpf.core.api.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Map/전문/JSON 값의 반복적인 안전 형변환을 줄이는 공개 utility입니다. */
public final class CpfValues {
    private CpfValues(){}
    public static String string(Object v){ return v==null?null:String.valueOf(v); }
    public static Integer integer(Object v){ if(v==null)return null; if(v instanceof Number n)return n.intValue(); return Integer.valueOf(String.valueOf(v).trim()); }
    public static Long longValue(Object v){ if(v==null)return null; if(v instanceof Number n)return n.longValue(); return Long.valueOf(String.valueOf(v).trim()); }
    public static BigDecimal decimal(Object v){ if(v==null)return null; if(v instanceof BigDecimal b)return b; return new BigDecimal(String.valueOf(v).trim()); }
    public static Boolean bool(Object v){ if(v==null)return null; if(v instanceof Boolean b)return b; String s=String.valueOf(v).trim(); if("Y".equalsIgnoreCase(s)||"1".equals(s)||"true".equalsIgnoreCase(s))return true; if("N".equalsIgnoreCase(s)||"0".equals(s)||"false".equalsIgnoreCase(s))return false; throw new IllegalArgumentException("Boolean으로 변환할 수 없습니다: "+s); }
    public static LocalDate date(Object v){ return v==null?null:CpfDates.parse(String.valueOf(v)); }
    public static Instant instant(Object v){ return v==null?null:Instant.parse(String.valueOf(v).trim()); }
}
