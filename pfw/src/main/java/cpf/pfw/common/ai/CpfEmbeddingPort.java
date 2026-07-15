package cpf.pfw.common.ai;

import java.util.List;

/** 텍스트를 벡터로 변환하는 공급자 독립 PFW embedding port입니다. */
public interface CpfEmbeddingPort {

    List<CpfEmbedding> embed(String model, List<String> texts);

    record CpfEmbedding(int index, List<Double> vector, long tokenCount) {
        public CpfEmbedding {
            vector = vector == null ? List.of() : List.copyOf(vector);
        }
    }
}
