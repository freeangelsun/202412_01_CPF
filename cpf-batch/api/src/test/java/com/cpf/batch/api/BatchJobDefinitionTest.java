package com.cpf.batch.api;
import org.junit.jupiter.api.Test;import java.util.List;import static org.junit.jupiter.api.Assertions.*;
class BatchJobDefinitionTest {
 @Test void shell_requires_catalog_reference(){assertThrows(IllegalArgumentException.class,()->new BatchJobDefinition("BAT.TEST",1,"test",BatchJobDefinition.ExecutorType.APPROVED_SHELL,null,"BAT","",new BatchJobDefinition.Trigger(BatchJobDefinition.TriggerType.MANUAL,"","Asia/Seoul",null,true),List.of(),List.of(),null,null,null,"/tmp/a.sh","","tester","승인된 배치 등록",null,null,0));}
 @Test void file_requires_path_alias(){assertThrows(IllegalArgumentException.class,()->new BatchJobDefinition("BAT.FILE",1,"file",BatchJobDefinition.ExecutorType.FILE_WATCH,null,"BAT","",new BatchJobDefinition.Trigger(BatchJobDefinition.TriggerType.FILE,"*.dat","Asia/Seoul",null,true),List.of(),List.of(),null,null,null,"FILE:INBOX","","tester","파일 배치 등록",null,null,0));}
}
