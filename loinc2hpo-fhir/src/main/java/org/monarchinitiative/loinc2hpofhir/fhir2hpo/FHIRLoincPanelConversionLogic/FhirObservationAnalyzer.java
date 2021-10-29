package org.monarchinitiative.loinc2hpofhir.fhir2hpo.FHIRLoincPanelConversionLogic;


import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.monarchinitiative.loinc2hpocore.Loinc2Hpo;
import org.monarchinitiative.loinc2hpocore.legacy.FhirCodeSystemConvertor;

import org.monarchinitiative.loinc2hpocore.annotationmodel.Hpo2Outcome;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotationModelLEGACY;
import org.monarchinitiative.loinc2hpocore.exception.Loinc2HpoRuntimeException;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.loinc2hpofhir.fhir2hpo.FhirResourceComponentFaker;
import org.monarchinitiative.loinc2hpofhir.fhir2hpo.ObservationAnalysisFromCodedValues;
import org.monarchinitiative.loinc2hpofhir.fhir2hpo.ObservationAnalysisFromInterpretation;
import org.monarchinitiative.loinc2hpofhir.fhir2hpo.ObservationAnalysisFromQnValue;
import org.monarchinitiative.loinc2hpofhir.phenotypemodel.BasicLabTestOutcome;
import org.monarchinitiative.loinc2hpofhir.phenotypemodel.LabTestOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is responsible for analyzing a FHIR observation
 * deprecated; use FhirObservation2Hpo
 */
@Deprecated
public class FhirObservationAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(FhirResourceComponentFaker.class);

    static private Observation observation;
    static private Set<LoincId> loincIds;
    static Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationMap;
    static FhirCodeSystemConvertor codeSystemConvertor;
    static Loinc2Hpo loinc2Hpo;

    /**
     * Initialize the resources required for observation to hpo transformation
     * @param loincIdsSet
     * @param loincAnnotationMap
     */
    public static void init(Set<LoincId> loincIdsSet, Map<LoincId, Loinc2HpoAnnotationModelLEGACY> loincAnnotationMap) {
        loincIds = loincIdsSet;
        annotationMap = loincAnnotationMap;
    }

    public static void setLoinc2Hpo(Loinc2Hpo exloinc2Hpo){
        loinc2Hpo = exloinc2Hpo;
    }

    public static void setObservation(Observation aFhirObservation) {
        observation = aFhirObservation;
        codeSystemConvertor = new FhirCodeSystemConvertor();
    }
    public static Observation getObservation(){ return observation; }

    public static LabTestOutcome getHPO4ObservationOutcome(Observation observationToAnalyze) throws FHIRException {
        observation = observationToAnalyze;
        return getHPO4ObservationOutcome(loincIds, annotationMap);
    }


    /**
     * A core function that tries three ways to return a LabTestOutcome object:
     * first, it tries to return the result through the interpretation field. If it fails,
     * second, it tries to return the result through the quantative value, or
     * third, it tries to return the retult through the coded value (ordinal Loinc)
     * @param loinc2HPOannotationMap
     * @param loincIds
     * @return
     */
    public static LabTestOutcome getHPO4ObservationOutcome(Set<LoincId> loincIds, Map<LoincId, Loinc2HpoAnnotationModelLEGACY> loinc2HPOannotationMap) throws  FHIRException {

        //first make sure the observation has a valid loinc code; otherwise, we cannot handle it
        if (!hasValidLoincCode(loincIds)) {
            //TODO: consider handling this as a future project
            throw Loinc2HpoRuntimeException.loincCodeNotFound();
        }

        LoincId loincId = getLoincIdOfObservation();

        if (!loinc2HPOannotationMap.containsKey(loincId)) {
            throw Loinc2HpoRuntimeException.loincCodeNotFound();
        }

        Hpo2Outcome hpoterm = null;
        if (observation.hasInterpretation()) {
            logger.debug("enter analyzer using the interpretation field");
            try {
                //hpoterm won't be null
                hpoterm = new ObservationAnalysisFromInterpretation(loinc2Hpo
                        , observation).getHPOforObservation();
//                        new ObservationAnalysisFromInterpretation(loincId, observation.getInterpretation(), loinc2HPOannotationMap, codeSystemConvertor).getHPOforObservation();
                return new BasicLabTestOutcome(hpoterm, null, observation.getSubject(), observation.getIdentifier());
            } catch (Loinc2HpoRuntimeException e) {
                logger.trace("Annotation for the interpretation code is not found; try other methods");
            } //catch (AmbiguousResultsFoundException e) {  //we should not catch this exception, I think --@azhang
            //  logger.trace("Interpretation code resulted conflicting hpo interpretation; try other methods");
            //}
        }

        //if we failed to analyze the outcome through the interpretation field, we try to analyze the raw value using
        //the reference range
        //Qn will have a value field
        if (observation.hasValueQuantity()) {
            hpoterm = new ObservationAnalysisFromQnValue(loinc2Hpo,
                    observation).getHPOforObservation();
//                    new ObservationAnalysisFromQnValue(loincId, observation, loinc2HPOannotationMap).getHPOforObservation();
            if (hpoterm != null) return new BasicLabTestOutcome(hpoterm, null, observation.getSubject(), observation.getIdentifier());
        }

        //Ord will have a ValueCodeableConcept field
        /*
        if (observation.hasValueCodeableConcept()) {
            hpoterm = new ObservationAnalysisFromCodedValues(loinc2Hpo,
                    observation).getHPOforObservation();
//                    new ObservationAnalysisFromCodedValues(loincId,
//                    observation.getValueCodeableConcept(), loinc2HPOannotationMap).getHPOforObservation();
            if (hpoterm != null) {
                return new BasicLabTestOutcome(hpoterm, null, observation.getSubject(), observation.getIdentifier());
            }
        }*/

        //@TODO: analyze observations with

        //if all the above fails, we cannot do nothing
        logger.error("Could not return HPO for observation: " + observation.getId());
        return new BasicLabTestOutcome(null, null, observation.getSubject(), observation.getIdentifier());
    }

    /**
     * Check whether a FHIR observation has a valid Loinc code
     * @param loincIds: a hashset of all loinc codes (just codes)
     * @return false if the observation does not have one, or in wrong format, or recognized in the hashset
     */
    private static boolean hasValidLoincCode(Set<LoincId> loincIds){

        for (Coding coding : observation.getCode().getCoding()) {
            if (coding.getSystem().equals("http://loinc.org")) {
                try {
                    LoincId loincId = new LoincId(coding.getCode());
                    if (!loincIds.contains(loincId)) {
                        logger.info("The observation has a correctly formed loinc code, but the code is not found in the loinc table. Check whether it is a new loinc code: " + loincId);
                    }
                    return loincIds.contains(loincId);
                } catch (Loinc2HpoRuntimeException e) {
                    logger.error("The loinc code is not formed correctly: " + coding.getCode());
                    return false;
                }
            }
        }
        logger.info("The observation does not have a loinc code. Observation ID: " + observation.getId());
        return false;
    }


    /**
     * A method to get the loinc id from a FHIR observation
     * @return

     */
    public static LoincId getLoincIdOfObservation() {
        LoincId loincId = null;
        for (Coding coding : observation.getCode().getCoding()) {
            if (coding.getSystem().equals("http://loinc.org")) {
                loincId = new LoincId(coding.getCode());
            }
        }
        if (loincId == null) throw Loinc2HpoRuntimeException.loincCodeNotFound();
        return loincId;
    }

    /**
     * Extract LOINC code strings from the observation.
     * Note: the function does not check the returned value conform LoincId format
     * @param observation
     * @return a list of LOINC codes
     */
    public static List<String> getLoincIdOfObservation(Observation observation) {
        return observation.getCode().getCoding().stream()
                .filter(c -> c.getSystem().equals("http://loinc.org"))
                .map(Coding::getCode)
                .collect(Collectors.toList());
    }

}
