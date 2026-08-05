package com.cpf.starter.sftp;

public final class S03SftpPolicyHarness {
    private static int cases;
    public static void main(String[] args) {
        check("FAILED".equals(CpfSftpUploadOutcomePolicy.failureStatus(false)), "pre-command failure");
        check("UNKNOWN".equals(CpfSftpUploadOutcomePolicy.failureStatus(true)), "post-command unknown");
        check("/a.dat.cpf-part-id1".equals(CpfSftpUploadOutcomePolicy.remoteWorkPath("/a.dat", "id1", false)), "atomic temp path");
        check("/a.dat".equals(CpfSftpUploadOutcomePolicy.remoteWorkPath("/a.dat", "id1", true)), "resume target");
        check(CpfSftpUploadOutcomePolicy.checksumMatches("aBcD", "ABCD"), "checksum normalization");
        check(!CpfSftpUploadOutcomePolicy.checksumMatches("abcd", "abce"), "checksum mismatch");
        check(!CpfSftpUploadOutcomePolicy.checksumMatches(null, "abcd"), "missing checksum fail closed");
        System.out.println("S03_SFTP_OUTCOME_HARNESS PASS cases=" + cases);
    }
    private static void check(boolean condition, String label) {
        cases++;
        if (!condition) throw new AssertionError(label);
    }
}
