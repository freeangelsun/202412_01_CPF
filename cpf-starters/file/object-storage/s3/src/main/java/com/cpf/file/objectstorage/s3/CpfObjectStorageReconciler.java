package com.cpf.file.objectstorage.s3;

import com.cpf.file.objectstorage.api.CpfObjectStorageOperations;
import org.springframework.scheduling.annotation.Scheduled;

/** Periodically aborts orphan multipart uploads; failed SDK calls remain visible and are retried on the next cycle. */
public final class CpfObjectStorageReconciler {
    private final CpfObjectStorageOperations operations;
    private final CpfObjectStorageProperties properties;
    public CpfObjectStorageReconciler(CpfObjectStorageOperations operations,CpfObjectStorageProperties properties){this.operations=operations;this.properties=properties;}
    @Scheduled(fixedDelayString="${cpf.file.object-storage.s3.reconcile-delay:PT10M}")
    public void reconcile(){operations.abortMultipartOlderThan(properties.getBucket(),properties.getOrphanMultipartAge());}
}
