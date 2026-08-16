package com.cpf.messaging.schema.runtime;


import com.cpf.messaging.schema.api.CpfEventSchemaCompatibility;
import com.cpf.messaging.schema.api.CpfEventSchemaCompatibilityResult;
import com.cpf.messaging.schema.api.CpfEventSchemaDescriptor;
import com.cpf.messaging.schema.api.CpfEventSchemaFormat;
import com.cpf.messaging.schema.api.CpfEventSchemaRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.avro.Schema;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;

/**
 * CPF 이벤트 스키마의 provider-neutral 인메모리 Registry 구현.
 * CI/로컬 검증과 단일 프로세스 기본 Runtime에서 동일한 호환성 규칙을 사용한다.
 */
public final class CpfInMemoryEventSchemaRegistry implements CpfEventSchemaRegistry {
    private final Map<String, List<CpfEventSchemaDescriptor>> subjects = new ConcurrentHashMap<>();
    private final Map<String, CpfEventSchemaDescriptor> ids = new ConcurrentHashMap<>();
    private final ObjectMapper json = new ObjectMapper();

    @Override
    public synchronized CpfEventSchemaDescriptor register(CpfEventSchemaDescriptor candidate) {
        validateDescriptor(candidate);
        var previous = latest(candidate.subject());
        if (previous.isPresent()) {
            var result = compatibility(previous.get(), candidate);
            if (!result.compatible()) {
                throw new IllegalArgumentException("breaking schema: " + result.violations());
            }
            if (candidate.version() != previous.get().version() + 1) {
                throw new IllegalArgumentException("schema version must increment by one");
            }
        } else if (candidate.version() != 1) {
            throw new IllegalArgumentException("first schema version must be 1");
        }
        if (ids.putIfAbsent(candidate.schemaId(), candidate) != null) {
            throw new IllegalArgumentException("duplicate schemaId: " + candidate.schemaId());
        }
        subjects.computeIfAbsent(candidate.subject(), key -> new ArrayList<>()).add(candidate);
        return candidate;
    }

    @Override
    public Optional<CpfEventSchemaDescriptor> latest(String subject) {
        var history = subjects.get(subject);
        return history == null || history.isEmpty() ? Optional.empty() : Optional.of(history.getLast());
    }

    @Override
    public Optional<CpfEventSchemaDescriptor> byId(String id) {
        return Optional.ofNullable(ids.get(id));
    }

    @Override
    public List<CpfEventSchemaDescriptor> history(String subject) {
        return List.copyOf(subjects.getOrDefault(subject, List.of()));
    }

    @Override
    public CpfEventSchemaCompatibilityResult compatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate) {
        var violations = new ArrayList<String>();
        if (previous.format() != candidate.format()) violations.add("format changed");
        if (!Objects.equals(previous.contentType(), candidate.contentType())) violations.add("contentType changed");
        if (candidate.canonicalSchema() == null || candidate.canonicalSchema().isBlank()) violations.add("definition empty");
        if (!violations.isEmpty()) return new CpfEventSchemaCompatibilityResult(false, violations);

        if (candidate.format() == CpfEventSchemaFormat.JSON_SCHEMA) {
            jsonRequiredCompatibility(previous, candidate, violations);
        } else if (candidate.format() == CpfEventSchemaFormat.AVRO) {
            avroCompatibility(previous, candidate, violations);
        } else if (candidate.format() == CpfEventSchemaFormat.PROTOBUF) {
            protobufCompatibility(previous, candidate, violations);
        }
        return new CpfEventSchemaCompatibilityResult(violations.isEmpty(), violations);
    }

    @Override
    public void validate(CpfEventSchemaDescriptor schema, byte[] payload) {
        Objects.requireNonNull(payload, "payload");
        try {
            switch (schema.format()) {
                case JSON_SCHEMA -> {
                    JsonNode document = json.readTree(payload);
                    JsonNode definition = json.readTree(schema.canonicalSchema());
                    for (String required : required(definition)) {
                        if (!document.has(required)) throw new IllegalArgumentException("required field missing: " + required);
                    }
                }
                case AVRO -> {
                    Schema avro = new Schema.Parser().parse(schema.canonicalSchema());
                    new GenericDatumReader<GenericRecord>(avro).read(null, DecoderFactory.get().binaryDecoder(payload, null));
                }
                case PROTOBUF -> {
                    var descriptor = protobufMessageDescriptor(schema);
                    DynamicMessage.parseFrom(descriptor, payload);
                }
            }
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new IllegalArgumentException("schema validation failed", e);
        }
    }

    private void validateDescriptor(CpfEventSchemaDescriptor schema) {
        Objects.requireNonNull(schema, "schema");
        if (schema.subject() == null || schema.subject().isBlank()) throw new IllegalArgumentException("subject required");
        if (schema.schemaId() == null || schema.schemaId().isBlank()) throw new IllegalArgumentException("schemaId required");
        if (schema.canonicalSchema() == null || schema.canonicalSchema().isBlank()) throw new IllegalArgumentException("definition required");
        if (schema.format() == CpfEventSchemaFormat.PROTOBUF) {
            try { protobufMessageDescriptor(schema); }
            catch (Exception e) { throw new IllegalArgumentException("invalid protobuf descriptor set/messageType", e); }
        }
    }

    private void jsonRequiredCompatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate, List<String> violations) {
        try {
            Set<String> oldRequired = required(json.readTree(previous.canonicalSchema()));
            Set<String> nextRequired = required(json.readTree(candidate.canonicalSchema()));
            if (candidate.compatibility() == CpfEventSchemaCompatibility.BACKWARD || candidate.compatibility() == CpfEventSchemaCompatibility.FULL) {
                var added = new HashSet<>(nextRequired); added.removeAll(oldRequired);
                if (!added.isEmpty()) violations.add("new required fields: " + added);
            }
            if (candidate.compatibility() == CpfEventSchemaCompatibility.FORWARD || candidate.compatibility() == CpfEventSchemaCompatibility.FULL) {
                var removed = new HashSet<>(oldRequired); removed.removeAll(nextRequired);
                if (!removed.isEmpty()) violations.add("removed required fields: " + removed);
            }
        } catch (Exception e) {
            violations.add("invalid json schema");
        }
    }

    private static Set<String> required(JsonNode node) {
        var values = new LinkedHashSet<String>();
        JsonNode required = node.path("required");
        if (required.isArray()) required.forEach(value -> values.add(value.asText()));
        return values;
    }

    private static void avroCompatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate, List<String> violations) {
        try {
            var oldSchema = new Schema.Parser().parse(previous.canonicalSchema());
            var nextSchema = new Schema.Parser().parse(candidate.canonicalSchema());
            var mode = candidate.compatibility();
            if ((mode == CpfEventSchemaCompatibility.BACKWARD || mode == CpfEventSchemaCompatibility.FULL)
                    && SchemaCompatibility.checkReaderWriterCompatibility(nextSchema, oldSchema).getType() != SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE) {
                violations.add("avro backward incompatibility");
            }
            if ((mode == CpfEventSchemaCompatibility.FORWARD || mode == CpfEventSchemaCompatibility.FULL)
                    && SchemaCompatibility.checkReaderWriterCompatibility(oldSchema, nextSchema).getType() != SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE) {
                violations.add("avro forward incompatibility");
            }
        } catch (Exception e) {
            violations.add("invalid avro schema");
        }
    }

    private static void protobufCompatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate, List<String> violations) {
        try {
            var oldMessage = protobufMessageDescriptor(previous);
            var nextMessage = protobufMessageDescriptor(candidate);
            Map<Integer, Descriptors.FieldDescriptor> oldFields = byNumber(oldMessage);
            Map<Integer, Descriptors.FieldDescriptor> nextFields = byNumber(nextMessage);
            for (var entry : oldFields.entrySet()) {
                var next = nextFields.get(entry.getKey());
                if (next != null && next.getType() != entry.getValue().getType()) {
                    violations.add("protobuf field type changed at tag " + entry.getKey());
                }
            }
        } catch (Exception e) {
            violations.add("invalid protobuf descriptor set");
        }
    }

    private static Map<Integer, Descriptors.FieldDescriptor> byNumber(Descriptors.Descriptor descriptor) {
        var result = new HashMap<Integer, Descriptors.FieldDescriptor>();
        descriptor.getFields().forEach(field -> result.put(field.getNumber(), field));
        return result;
    }

    private static Descriptors.Descriptor protobufMessageDescriptor(CpfEventSchemaDescriptor schema)
            throws IOException, Descriptors.DescriptorValidationException {
        String messageType = schema.metadata().get("messageType");
        if (messageType == null || messageType.isBlank()) throw new IllegalArgumentException("protobuf metadata.messageType required");
        byte[] bytes = Base64.getDecoder().decode(schema.canonicalSchema());
        FileDescriptorSet set = FileDescriptorSet.parseFrom(bytes);
        Map<String, FileDescriptorProto> protos = new HashMap<>();
        set.getFileList().forEach(file -> protos.put(file.getName(), file));
        Map<String, Descriptors.FileDescriptor> built = new HashMap<>();
        for (FileDescriptorProto file : set.getFileList()) buildFile(file.getName(), protos, built);
        for (Descriptors.FileDescriptor file : built.values()) {
            var found = findMessage(file.getMessageTypes(), messageType);
            if (found != null) return found;
        }
        throw new IllegalArgumentException("protobuf messageType not found: " + messageType);
    }

    private static Descriptors.FileDescriptor buildFile(String name, Map<String, FileDescriptorProto> protos,
                                                         Map<String, Descriptors.FileDescriptor> built)
            throws Descriptors.DescriptorValidationException {
        var existing = built.get(name); if (existing != null) return existing;
        FileDescriptorProto proto = Objects.requireNonNull(protos.get(name), "missing protobuf dependency " + name);
        var deps = new ArrayList<Descriptors.FileDescriptor>();
        for (String dependency : proto.getDependencyList()) deps.add(buildFile(dependency, protos, built));
        var descriptor = Descriptors.FileDescriptor.buildFrom(proto, deps.toArray(Descriptors.FileDescriptor[]::new));
        built.put(name, descriptor); return descriptor;
    }

    private static Descriptors.Descriptor findMessage(List<Descriptors.Descriptor> candidates, String fullName) {
        for (var candidate : candidates) {
            if (candidate.getFullName().equals(fullName)) return candidate;
            var nested = findMessage(candidate.getNestedTypes(), fullName);
            if (nested != null) return nested;
        }
        return null;
    }
}
