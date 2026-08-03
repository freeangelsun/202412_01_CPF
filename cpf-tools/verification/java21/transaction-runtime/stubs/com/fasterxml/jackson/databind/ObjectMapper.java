package com.fasterxml.jackson.databind; import java.io.*; public class ObjectMapper { public void writeValue(Writer w,Object v)throws IOException{w.write(String.valueOf(v));} }
