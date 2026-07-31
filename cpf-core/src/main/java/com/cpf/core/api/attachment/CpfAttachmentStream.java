package com.cpf.core.api.attachment;
import java.io.IOException;import java.io.InputStream;import java.util.Objects;
/** 호출자가 반드시 close해야 하는 bounded attachment stream입니다. */
public record CpfAttachmentStream(InputStream inputStream,long size,String checksumSha256) implements AutoCloseable{
 public CpfAttachmentStream{Objects.requireNonNull(inputStream,"inputStream");if(size<0)throw new IllegalArgumentException("size");}
 @Override public void close()throws IOException{inputStream.close();}
}
