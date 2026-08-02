package com.cpf.tools.release;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;

/** Minimal detached Ed25519 signer/verifier using JDK standard crypto. */
public final class CpfReleaseSigner {
    private static final int STREAM_BUFFER_BYTES = 64 * 1024;
    private static final int MAX_KEY_PEM_BYTES = 64 * 1024;
    private static final int MAX_SIGNATURE_TEXT_BYTES = 4 * 1024;

    private CpfReleaseSigner() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 4 || !(args[0].equals("sign") || args[0].equals("verify"))) {
            throw new IllegalArgumentException("usage: sign|verify <key.pem> <input> <signature.base64>");
        }
        if (args[0].equals("sign")) sign(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
        else verify(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
    }
    static void sign(Path keyFile, Path input, Path output) throws Exception {
        byte[] encodedKey = readPem(keyFile,"PRIVATE KEY");
        PrivateKey key;
        try {
            key = KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } finally {
            Arrays.fill(encodedKey, (byte) 0);
        }
        Signature signature = Signature.getInstance("Ed25519"); signature.initSign(key); update(signature, input);
        Files.writeString(output, Base64.getEncoder().encodeToString(signature.sign()) + System.lineSeparator());
    }
    static void verify(Path keyFile, Path input, Path signatureFile) throws Exception {
        byte[] encodedKey = readPem(keyFile,"PUBLIC KEY");
        PublicKey key;
        try {
            key = KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(encodedKey));
        } finally {
            Arrays.fill(encodedKey, (byte) 0);
        }
        Signature signature = Signature.getInstance("Ed25519"); signature.initVerify(key); update(signature, input);
        byte[] detachedSignature = Base64.getDecoder().decode(readBoundedText(signatureFile, MAX_SIGNATURE_TEXT_BYTES).trim());
        try {
            if (!signature.verify(detachedSignature)) throw new SignatureException("Ed25519 verification failed: "+input);
        } finally {
            Arrays.fill(detachedSignature, (byte) 0);
        }
    }
    private static byte[] readPem(Path path,String label) throws Exception {
        String text=readBoundedText(path, MAX_KEY_PEM_BYTES).replace("-----BEGIN "+label+"-----","").replace("-----END "+label+"-----","").replaceAll("\\s","");
        return Base64.getDecoder().decode(text);
    }
    private static String readBoundedText(Path path, int maxBytes) throws Exception {
        byte[] bytes;
        try (InputStream stream = Files.newInputStream(path)) {
            bytes = stream.readNBytes(maxBytes + 1);
        }
        try {
            if (bytes.length > maxBytes) throw new IllegalArgumentException("bounded text input exceeds " + maxBytes + " bytes: " + path);
            return new String(bytes, StandardCharsets.UTF_8);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }
    private static void update(Signature signature, Path input) throws Exception {
        byte[] buffer = new byte[STREAM_BUFFER_BYTES];
        try (InputStream stream = Files.newInputStream(input)) {
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                if (read > 0) signature.update(buffer, 0, read);
            }
        } finally {
            Arrays.fill(buffer, (byte) 0);
        }
    }
}
