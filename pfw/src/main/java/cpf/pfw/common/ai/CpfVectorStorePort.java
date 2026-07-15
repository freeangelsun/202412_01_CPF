package cpf.pfw.common.ai;

import java.util.List;
import java.util.Map;

/** RAG 검색 저장소를 제품별 구현과 분리하는 PFW vector store port입니다. */
public interface CpfVectorStorePort {

    void upsert(List<CpfVectorDocument> documents);

    List<CpfVectorMatch> search(List<Double> queryVector, int limit, Map<String, String> filters);

    record CpfVectorDocument(
            String documentId,
            String title,
            String content,
            List<Double> vector,
            Map<String, String> metadata) {
        public CpfVectorDocument {
            vector = vector == null ? List.of() : List.copyOf(vector);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    record CpfVectorMatch(
            String documentId,
            String title,
            String content,
            double score,
            Map<String, String> metadata) {
        public CpfVectorMatch {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }
}
