package org.monarchinitiative.loinc2hpofhir.phenotypemodel;

import org.jgrapht.alg.util.UnionFind;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Hpo2Outcome;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotationModelLEGACY;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.TermId;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use a UnionFind structure to represent disjoint sets of phenotypes, of
 * which only one phenotype can be assigned to a patient at any given time.
 * For example, Hyperglycemia, Hypoglycemia, Not Abnormal blood glucose
 * concentration is in one set.
 */
public class PhenoSetUnionFind {
    private final Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationMap;
    private final UnionFind<TermId> unionFind;

    public PhenoSetUnionFind(Set<TermId> hpoTermSet, Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationMap) {
        this.annotationMap = annotationMap;
        this.unionFind = new UnionFind<>(hpoTermSet);
        union(this.annotationMap);
    }

    private void union(Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationMap) {
        annotationMap.values().forEach(annotation -> {
            List<TermId> termInAnnot = annotation.getCandidateHpoTerms().values()
                    .stream()
                    .map(Hpo2Outcome::getId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!termInAnnot.isEmpty() && termInAnnot.size() > 1) {
                //union terms in this list
                //after this, all terms that have shared LOINC tests are unioned
                TermId firstTerm = termInAnnot.get(0);
                termInAnnot.stream().skip(1).forEach(a -> this.unionFind.union(firstTerm, a));
            }
        });
    }

    public UnionFind<TermId> getUnionFind() {
        return this.unionFind;
    }

}
