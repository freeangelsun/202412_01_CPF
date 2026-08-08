package com.cpf.starter.attachment;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
class CpfAttachmentUploadPolicyTest {
 @Test void rejectsTraversalAndOversize(){var p=new CpfAttachmentUploadPolicy(10,Set.of("text/plain"),Set.of("txt"));assertThrows(IllegalArgumentException.class,()->p.validate("../a.txt","text/plain",1));assertThrows(IllegalArgumentException.class,()->p.validate("a.txt","text/plain",11));}
 @Test void acceptsAllowedFile(){var p=new CpfAttachmentUploadPolicy(10,Set.of("text/plain"),Set.of("txt"));var m=p.validate("a.txt","text/plain",10);assertEquals("a.txt",m.filename());}
}
