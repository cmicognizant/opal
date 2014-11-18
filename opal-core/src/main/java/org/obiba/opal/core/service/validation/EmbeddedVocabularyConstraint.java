package org.obiba.opal.core.service.validation;

import org.obiba.magma.Category;
import org.obiba.magma.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * Impl of VocabularyConstraint that takes codes from the categories.
 */
public class EmbeddedVocabularyConstraint extends VocabularyConstraint {

    public EmbeddedVocabularyConstraint(Variable variable) {
        super("category vocabulary", getVocabularyCodes(variable));
    }

    private static final Set<String> getVocabularyCodes(Variable variable) {
        Set<String> result = new HashSet<>();
        for (Category cat: variable.getCategories()) {
            result.add(cat.getCode());
        }
        return result;
    }

}