package com.cpf.starter.integration.fixedlength;
import java.math.BigInteger;import java.nio.*;import java.util.HexFormat;
/** Strict binary field codec for unsigned integers, endian conversion, packed BCD and hexadecimal fields. */
public final class CpfBinaryFieldCodec {
 public byte[] unsigned(long value,int width,ByteOrder order){if(width<1||width>8||value<0)throw new IllegalArgumentException("unsigned width/value");BigInteger max=BigInteger.ONE.shiftLeft(width*8);BigInteger n=BigInteger.valueOf(value);if(n.compareTo(max)>=0)throw new IllegalArgumentException("unsigned overflow");byte[] out=new byte[width];for(int i=0;i<width;i++){int index=order==ByteOrder.BIG_ENDIAN?width-1-i:i;out[index]=(byte)(value >>> (i*8));}return out;}
 public long unsigned(byte[] bytes,ByteOrder order){if(bytes.length<1||bytes.length>8)throw new IllegalArgumentException("unsigned width");long value=0;for(int i=0;i<bytes.length;i++){int index=order==ByteOrder.BIG_ENDIAN?i:bytes.length-1-i;value=(value<<8)|(bytes[index]&0xffL);}if(value<0)throw new ArithmeticException("unsigned value exceeds signed long");return value;}
 public byte[] packedBcd(String digits){if(digits==null||!digits.matches("[0-9]+"))throw new IllegalArgumentException("BCD digits required");String normalized=(digits.length()%2==0?digits:"0"+digits);byte[] out=new byte[normalized.length()/2];for(int i=0;i<out.length;i++)out[i]=(byte)(((normalized.charAt(i*2)-'0')<<4)|(normalized.charAt(i*2+1)-'0'));return out;}
 public String packedBcd(byte[] bytes,int digits){StringBuilder b=new StringBuilder(bytes.length*2);for(byte v:bytes)b.append((char)('0'+((v>>>4)&0xf))).append((char)('0'+(v&0xf)));String value=b.toString();if(!value.matches("[0-9]+")||digits<1||digits>value.length())throw new IllegalArgumentException("invalid BCD");return value.substring(value.length()-digits);}
 public byte[] hex(String value){try{return HexFormat.of().parseHex(value);}catch(IllegalArgumentException ex){throw new IllegalArgumentException("invalid hexadecimal field",ex);}}
 public String hex(byte[] value){return HexFormat.of().withUpperCase().formatHex(value);}
}
