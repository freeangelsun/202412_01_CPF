package com.cpf.file.common.filetransfer;

import com.cpf.file.spi.filetransfer.CpfFileInspectionPort;
import com.cpf.file.spi.filetransfer.CpfFileTransferEndpoint;
import com.cpf.file.spi.filetransfer.CpfFileTransferRequest;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** File/SFTP Runtime Control의 immutable endpoint/policy snapshot입니다. */
public final class CpfFileTransferRuntimeState {
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.defaults());
    public Snapshot current(){return snapshot.get();}
    public Snapshot replacePolicy(FilePolicy policy){return snapshot.updateAndGet(s->new Snapshot(s.version()+1,s.endpoints(),policy));}
    public Snapshot replaceEndpoints(long version, Map<String,CpfFileTransferEndpoint> endpoints){
        if(version<0)throw new IllegalArgumentException("version 범위 오류");
        return snapshot.updateAndGet(s->new Snapshot(version,normalizeEndpoints(endpoints),s.filePolicy()));
    }
    public CpfFileTransferEndpoint resolve(CpfFileTransferEndpoint requested){
        if(requested==null)throw new IllegalArgumentException("endpoint 필수");
        return snapshot.get().endpoints().getOrDefault(requested.endpointCode().trim().toUpperCase(Locale.ROOT),requested);
    }
    public void validate(CpfFileTransferRequest request,CpfFileInspectionPort inspectionPort){
        FilePolicy p=snapshot.get().filePolicy();
        if(request.fileSize()<0||request.fileSize()>p.maxFileSize())throw new IllegalArgumentException("FILE_SIZE_POLICY_REJECTED");
        String path=request.localPath()==null?request.remotePath():request.localPath();
        String ext=extension(path);
        if(!p.allowedExtensions().isEmpty()&&!p.allowedExtensions().contains(ext))throw new IllegalArgumentException("FILE_EXTENSION_POLICY_REJECTED");
        String mime=request.attributes().getOrDefault("mimeType","").toLowerCase(Locale.ROOT);
        if(!p.allowedMimeTypes().isEmpty()&&!p.allowedMimeTypes().contains(mime))throw new IllegalArgumentException("FILE_MIME_POLICY_REJECTED");
        if(p.checksumRequired()&&(request.checksum()==null||request.checksum().isBlank()))throw new IllegalArgumentException("FILE_CHECKSUM_REQUIRED");
        if(p.scanRequired()){
            if(inspectionPort==null)throw new IllegalStateException("FILE_SCAN_PORT_REQUIRED");
            CpfFileInspectionPort.Result result=inspectionPort.inspect(request,p.quarantineOnFailure());
            if(result==null||!result.accepted())throw new IllegalArgumentException("FILE_SCAN_REJECTED");
        }
    }
    private String extension(String path){if(path==null)return "";int slash=Math.max(path.lastIndexOf('/'),path.lastIndexOf('\\'));int dot=path.lastIndexOf('.');return dot>slash?path.substring(dot+1).toLowerCase(Locale.ROOT):"";}
    private static Map<String,CpfFileTransferEndpoint> normalizeEndpoints(Map<String,CpfFileTransferEndpoint> source){LinkedHashMap<String,CpfFileTransferEndpoint>r=new LinkedHashMap<>();if(source!=null)source.forEach((k,v)->{if(v!=null)r.put(v.endpointCode().trim().toUpperCase(Locale.ROOT),v);});return Map.copyOf(r);}
    public record Snapshot(long version,Map<String,CpfFileTransferEndpoint>endpoints,FilePolicy filePolicy){private static Snapshot defaults(){return new Snapshot(0,Map.of(),FilePolicy.defaults());}}
    public record FilePolicy(long maxFileSize,Set<String>allowedExtensions,Set<String>allowedMimeTypes,boolean checksumRequired,boolean scanRequired,boolean quarantineOnFailure){
        public FilePolicy{if(maxFileSize<1)throw new IllegalArgumentException("maxFileSize 범위 오류");allowedExtensions=normalize(allowedExtensions);allowedMimeTypes=normalize(allowedMimeTypes);}
        private static FilePolicy defaults(){return new FilePolicy(1024L*1024L*1024L,Set.of(),Set.of(),true,false,true);}
        private static Set<String>normalize(Set<String>s){if(s==null)return Set.of();return s.stream().filter(v->v!=null&&!v.isBlank()).map(v->v.trim().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());}
    }
}
