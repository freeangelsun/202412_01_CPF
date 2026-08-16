package com.cpf.batch.agent;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class ApprovedCommandCatalogTest {@Test void arbitraryShellIsRejected(){ApprovedCommandCatalog c=new ApprovedCommandCatalog();assertThrows(SecurityException.class,()->c.requireAllowed("SHELL"));assertDoesNotThrow(()->c.requireAllowed("ROLLBACK"));}}
