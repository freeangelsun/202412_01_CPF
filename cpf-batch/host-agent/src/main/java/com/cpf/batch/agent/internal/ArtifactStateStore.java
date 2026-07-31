package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Artifact active/previous state를 HMAC으로 보호하고 원자적으로 publish합니다. */
public final class ArtifactStateStore {
    private static final String MAC_PROPERTY = "stateMac";
    private final byte[] macKey;

    public ArtifactStateStore(AgentProperties properties) {
        String encoded = properties.getArtifactStateMacKeyBase64();
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException("cpf.agent.artifact-state-mac-key-base64 is required");
        }
        try { macKey = Base64.getDecoder().decode(encoded); }
        catch (IllegalArgumentException failure) { throw new IllegalStateException("Artifact state MAC key is not valid Base64", failure); }
        if (macKey.length < 32) throw new IllegalStateException("Artifact state MAC key must be at least 256 bits");
    }

    public Properties read(Path path, boolean required) throws Exception {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (required) throw new IllegalStateException("ARTIFACT_STATE_MISSING:" + path.getFileName());
            return new Properties();
        }
        requireSecureFile(path);
        Properties stored = new Properties();
        try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) { stored.load(input); }
        String expected = stored.getProperty(MAC_PROPERTY, "");
        if (!expected.matches("[0-9a-f]{64}")) throw new SecurityException("ARTIFACT_STATE_MAC_MISSING");
        Properties unsigned = copyWithoutMac(stored);
        String actual = mac(unsigned);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), actual.getBytes(StandardCharsets.US_ASCII))) {
            throw new SecurityException("ARTIFACT_STATE_MAC_INVALID");
        }
        return unsigned;
    }

    public void write(Path path, Properties state) throws Exception {
        Properties signed = copyWithoutMac(state);
        signed.setProperty(MAC_PROPERTY, mac(signed));
        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IllegalArgumentException("Artifact state parent is missing");
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, path.getFileName() + ".", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary, StandardOpenOption.TRUNCATE_EXISTING)) {
                signed.store(output, "CPF tamper-evident artifact state");
            }
            setOwnerOnly(temporary);
            try (FileChannel file = FileChannel.open(temporary, StandardOpenOption.READ)) { file.force(true); }
            move(temporary, path);
            setOwnerOnly(path);
            try (FileChannel directory = FileChannel.open(parent, StandardOpenOption.READ)) { directory.force(true); }
            catch (Exception ignored) { /* Windows and some file systems do not support directory fsync. */ }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String mac(Properties properties) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(macKey, "HmacSHA256"));
        return java.util.HexFormat.of().formatHex(mac.doFinal(canonical(properties).getBytes(StandardCharsets.UTF_8)));
    }

    private static String canonical(Properties properties) {
        List<String> names = new ArrayList<>(properties.stringPropertyNames());
        names.remove(MAC_PROPERTY);
        names.sort(Comparator.naturalOrder());
        StringBuilder result = new StringBuilder("cpf-artifact-state-v1\n");
        for (String name : names) {
            String value = properties.getProperty(name, "");
            if (name.indexOf('\n') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                throw new SecurityException("ARTIFACT_STATE_FIELD_INVALID");
            }
            result.append(name).append('=').append(value).append('\n');
        }
        return result.toString();
    }

    private static Properties copyWithoutMac(Properties source) {
        Properties copy = new Properties();
        for (String name : source.stringPropertyNames()) {
            if (!MAC_PROPERTY.equals(name)) copy.setProperty(name, source.getProperty(name, ""));
        }
        return copy;
    }

    private static void requireSecureFile(Path path) throws Exception {
        if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("ARTIFACT_STATE_FILE_UNSAFE");
        }
        try {
            var permissions = Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
            if (permissions.contains(PosixFilePermission.GROUP_READ)
                    || permissions.contains(PosixFilePermission.GROUP_WRITE)
                    || permissions.contains(PosixFilePermission.GROUP_EXECUTE)
                    || permissions.contains(PosixFilePermission.OTHERS_READ)
                    || permissions.contains(PosixFilePermission.OTHERS_WRITE)
                    || permissions.contains(PosixFilePermission.OTHERS_EXECUTE)) {
                throw new SecurityException("ARTIFACT_STATE_PERMISSION_UNSAFE");
            }
            return;
        } catch (UnsupportedOperationException ignored) {
            // Windows/NTFS 경로는 아래 ACL 검증으로 전환합니다.
        }
        AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) throw new SecurityException("ARTIFACT_STATE_ACL_UNSUPPORTED");
        UserPrincipal owner = Files.getOwner(path, LinkOption.NOFOLLOW_LINKS);
        for (AclEntry entry : view.getAcl()) {
            if (entry.type() == AclEntryType.ALLOW && !entry.principal().equals(owner)
                    && !entry.permissions().isEmpty()) {
                throw new SecurityException("ARTIFACT_STATE_ACL_UNSAFE:" + entry.principal().getName());
            }
        }
    }

    private static void setOwnerOnly(Path path) throws Exception {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            return;
        } catch (UnsupportedOperationException ignored) {
            // Windows/NTFS 경로는 ACL을 명시적으로 덮어씁니다.
        }
        AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) throw new SecurityException("ARTIFACT_STATE_ACL_UNSUPPORTED");
        UserPrincipal owner = Files.getOwner(path, LinkOption.NOFOLLOW_LINKS);
        AclEntry ownerOnly = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .build();
        view.setAcl(List.of(ownerOnly));
    }

    private static void move(Path source, Path target) throws Exception {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException failure) {
            throw new IllegalStateException("ARTIFACT_STATE_ATOMIC_MOVE_REQUIRED", failure);
        }
    }
}
