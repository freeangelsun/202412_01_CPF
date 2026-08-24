package com.cpf.batch.control;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.batch.api.JobPackManifest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Transactional;

class JobPackRegistryControllerTransactionTest {
    @Test
    void replacesParentAndChildrenInOneRollbackCapableTransaction() throws Exception {
        Method registration = JobPackRegistryController.class.getMethod(
                "register", JobPackManifest.class);

        Transactional transaction = AnnotatedElementUtils.findMergedAnnotation(
                registration, Transactional.class);

        assertThat(transaction).isNotNull();
        assertThat(transaction.rollbackFor()).contains(Exception.class);
    }
}
