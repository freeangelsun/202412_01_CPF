package com.cpf.tools.release;

import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/** Minimal detached Ed25519 signer/verifier using JDK standard crypto. */
public final class CpfReleaseSigner {
    private CpfReleaseSigner() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 4 || !(args[0].equals("sign") || args[0].equals("verify"))) {
            throw new IllegalArgumentException("usage: sign|verify <key.pem> <input> <signature.base64>");
        }
        if (args[0].equals("sign")) sign(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
        else verify(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
    }
    static void sign(Path keyFile, Path input, Path output) throws Exception {
        PrivateKey key = KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(readPem(keyFile,"PRIVATE KEY")));
        Signature signature = Signature.getInstance("Ed25519"); signature.initSign(key); signature.update(Files.readAllBytes(input));
        Files.writeString(output, Base64.getEncoder().encodeToString(signature.sign()) + System.lineSeparator());
    }
    static void verify(Path keyFile, Path input, Path signatureFile) throws Exception {
        PublicKey key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(readPem(keyFile,"PUBLIC KEY")));
        Signature signature = Signature.getInstance("Ed25519"); signature.initVerify(key); signature.update(Files.readAllBytes(input));
        if (!signature.verify(Base64.getDecoder().decode(Files.readString(signatureFile).trim()))) throw new SignatureException("Ed25519 verification failed: "+input);
    }
    private static byte[] readPem(Path path,String label) throws Exception {
        String text=Files.readString(path).replace("-----BEGIN "+label+"-----","").replace("-----END "+label+"-----","").replaceAll("\\s","");
        return Base64.getDecoder().decode(text);
    }
}
