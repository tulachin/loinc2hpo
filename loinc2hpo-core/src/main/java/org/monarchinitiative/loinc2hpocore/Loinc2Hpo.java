package org.monarchinitiative.loinc2hpocore;


import org.monarchinitiative.loinc2hpocore.codesystems.Code;
import org.monarchinitiative.loinc2hpocore.codesystems.CodeSystemConvertor;
import org.monarchinitiative.loinc2hpocore.annotationmodel.HpoTerm4TestOutcome;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotationModel;
import org.monarchinitiative.loinc2hpocore.exception.Loinc2HpoRuntimeException;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Entry point for the Loinc2Hpo tool
 * @author <a href="mailto:aaron.zhang@sema4.com">Aaron Zhang</a>
 * @version 1.1.7
 */
public class Loinc2Hpo {
    private static final Logger logger = LoggerFactory.getLogger(Loinc2Hpo.class);

    private final Map<LoincId, Loinc2HpoAnnotationModel> annotationMap;
    private final CodeSystemConvertor converter;

    public Loinc2Hpo(Map<LoincId, Loinc2HpoAnnotationModel> annotationMap,
                     CodeSystemConvertor converter){
        this.annotationMap = annotationMap;
        this.converter = converter;
    }

    public Loinc2Hpo(String path, CodeSystemConvertor converter){
        try {
            annotationMap = Loinc2HpoAnnotationModel.from_csv(path);
        } catch (Exception e) {
            logger.error("Failed to import loinc2hpo annotation");
            throw new RuntimeException("failed to import loinc2hpo annotation");
        }
        this.converter = converter;
    }

    public Map<LoincId, Loinc2HpoAnnotationModel> getAnnotationMap() {
        return annotationMap;
    }

    public CodeSystemConvertor getConverter() {
        return converter;
    }

    public Code convertToInternal(Code original) {
        return this.converter.convertToInternalCode(original);
    }

    public HpoTerm4TestOutcome query(LoincId loincId, Code testResult)  {
        //The loinc id is not annotated yet
        if (!this.annotationMap.containsKey(loincId)) {
            throw Loinc2HpoRuntimeException.notAnnotated(loincId);
        }

        //The result code is not annotated
        if (!this.annotationMap.get(loincId).getCandidateHpoTerms().containsKey(testResult)){
            throw Loinc2HpoRuntimeException.notAnnotated(loincId);
        }

        return this.annotationMap.get(loincId).getCandidateHpoTerms().get(testResult);
    }

    public HpoTerm4TestOutcome query(LoincId loincId, String system, String id) {
        Code code = Code.fromSystemAndCode(system, id);
        return query(loincId, code);
    }


}
