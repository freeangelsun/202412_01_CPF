package com.cpf.integration.graphql;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.parser.Parser;
import java.util.ArrayList;
import java.util.List;

/** GraphQL AST에서 선택 field path만 추출해 field authorization에 사용한다. */
final class CpfGraphqlDocumentFields {
    private CpfGraphqlDocumentFields() { }
    static List<String> paths(String document) {
        if (document == null || document.isBlank()) return List.of();
        Document parsed = new Parser().parseDocument(document);
        List<String> result = new ArrayList<>();
        for (OperationDefinition operation : parsed.getDefinitionsOfType(OperationDefinition.class)) {
            collect(operation.getSelectionSet(), "", result);
        }
        return List.copyOf(result);
    }
    private static void collect(SelectionSet set, String parent, List<String> result) {
        if (set == null) return;
        for (Selection<?> selection : set.getSelections()) {
            if (selection instanceof Field field) {
                String path = parent.isEmpty() ? field.getName() : parent + "." + field.getName();
                result.add(path);
                collect(field.getSelectionSet(), path, result);
            }
        }
    }
}
