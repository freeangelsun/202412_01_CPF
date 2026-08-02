package com.cpf.core.common.filetransfer;

import com.cpf.core.api.filetransfer.CpfCredentialReference;
import com.cpf.core.api.filetransfer.CpfFileEndpoint;
import com.cpf.core.api.filetransfer.CpfFileRequest;
import com.cpf.core.api.filetransfer.CpfFileResult;
import com.cpf.core.common.security.CpfCredentialRef;

/** Public 파일전송 API를 기존 Runtime Engine에 연결하는 내부 adapter입니다. */
public final class CpfFileTransferClientAdapter implements com.cpf.core.api.filetransfer.CpfFileTransferClient {
    private final CpfFileTransferEngine engine;
    public CpfFileTransferClientAdapter(CpfFileTransferEngine engine){this.engine=java.util.Objects.requireNonNull(engine);}
    @Override public CpfFileResult execute(CpfFileEndpoint e,CpfFileRequest r){
        CpfCredentialReference c=e.credential();
        CpfCredentialRef credential=c==null?null:new CpfCredentialRef(c.scope(),c.credentialId(),c.version(),c.displayName());
        CpfFileTransferEndpoint ie=new CpfFileTransferEndpoint(e.endpointCode(),e.protocol(),e.host(),e.port(),e.remoteBasePath(),credential,e.timeout(),e.attributes());
        CpfFileTransferRequest ir=new CpfFileTransferRequest(r.transactionId(),r.segmentId(),r.endpointCode(),r.operation(),r.localPath(),r.remotePath(),r.checksum(),r.fileSize(),r.attributes());
        CpfFileTransferResult x=engine.execute(ie,ir);
        return new CpfFileResult(x.status(),x.endpointCode(),x.localPath(),x.remotePath(),x.checksum(),x.fileSize(),x.completedAt(),x.detail());
    }
}
