package com.cpf.file.archive;
import java.io.*;import java.nio.file.*;import java.util.zip.*;import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;import static org.assertj.core.api.Assertions.*;
class CpfArchiveServiceTest {@TempDir Path temp;
 @Test void roundTripsSafeArchive()throws Exception{Path base=Files.createDirectories(temp.resolve("src"));Path file=Files.writeString(base.resolve("a.txt"),"ok");Path zip=temp.resolve("a.zip");var service=new CpfArchiveService(new CpfArchiveProperties());service.zip(zip,java.util.List.of(file),base);assertThat(Files.readString(service.unzip(zip,temp.resolve("out")).getFirst())).isEqualTo("ok");}
 @Test void blocksZipSlip()throws Exception{Path zip=temp.resolve("bad.zip");try(var out=new ZipOutputStream(Files.newOutputStream(zip))){out.putNextEntry(new ZipEntry("../escape.txt"));out.write(1);out.closeEntry();}assertThatThrownBy(()->new CpfArchiveService(new CpfArchiveProperties()).unzip(zip,temp.resolve("out"))).isInstanceOf(IOException.class);}
}
