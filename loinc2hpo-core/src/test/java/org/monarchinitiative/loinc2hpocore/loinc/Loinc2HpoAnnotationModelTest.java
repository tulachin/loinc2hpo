package org.monarchinitiative.loinc2hpocore.loinc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotationModelLEGACY;
import org.monarchinitiative.loinc2hpocore.annotationmodel.Loinc2HpoAnnotationEntryLEGACY;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class Loinc2HpoAnnotationModelTest {
/*
    private static Map<String, Term> hpoTermMap = new HashMap<>();

    @BeforeAll
    public static void setUp(){

        List<Term> terms = Stream.of(
                Term.of("HP:0001943", "Hypoglycemia"),
                Term.of("HP:0011015", "Abnormality of blood glucose " +
                        "concentration"),
                Term.of("HP:0003074", "Hyperglycemia"),
                Term.of("HP:0002740", "Recurrent E. coli infections" ),
                Term.of("HP:0002726", "Recurrent Staphylococcus aureus infections"),
                Term.of("HP:0002718", "Recurrent bacterial infections")
        ).collect(Collectors.toList());

        hpoTermMap = terms.stream().collect(Collectors.toMap(Term::getName,
                t-> t));
    }


    @Test
    void to_csv_entries() throws Exception {
        Loinc2HpoAnnotationModelLEGACY loinc2HpoAnnotation =
                new Loinc2HpoAnnotationModelLEGACY.Builder()
                        .setLoincId(new LoincId("123-4"))
                        .setLoincScale(LoincScale.Qn)
                        .setCreatedBy("jax:azhang")
                        .setLowValueHpoTerm(TermId.of("HP:0012"))
                        .setHighValueHpoTerm(TermId.of("HP:00013"))
                        .setIntermediateValueHpoTerm(TermId.of("HP:00014"),
                                true)
                        .build();
        List<Loinc2HpoAnnotationEntryLEGACY> csvEntryList =
                Loinc2HpoAnnotationModelLEGACY.to_csv_entries(loinc2HpoAnnotation);
        assertEquals(3, csvEntryList.size());
        int distinctIdCount = (int)csvEntryList.stream()
                .map(Loinc2HpoAnnotationEntryLEGACY::getLoincId)
                .distinct().count();
        assertEquals(1, distinctIdCount);
        assertEquals(1,
                csvEntryList.stream().map(Loinc2HpoAnnotationEntryLEGACY::getLoincScale).distinct().count());
        assertEquals(3,
                csvEntryList.stream().map(Loinc2HpoAnnotationEntryLEGACY::getCode).distinct().count());

    }

    @Test
    void from_csv() throws Exception {
        String annotationPath = this.getClass().getClassLoader().getResource("annotations.tsv").getPath();
        Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationModelMap =
                Loinc2HpoAnnotationModelLEGACY.from_csv(annotationPath);
        assertTrue(annotationModelMap.size() > 100);
    }

    @Test
    void test_re_deserialize() throws Exception{
        String annotationPath = this.getClass().getClassLoader().getResource("annotations.tsv").getPath();
        Map<LoincId, Loinc2HpoAnnotationModelLEGACY> annotationModelMap =
                Loinc2HpoAnnotationModelLEGACY.from_csv(annotationPath);

        List<String> lines_to_write = annotationModelMap.values().stream()
                .map(Loinc2HpoAnnotationModelLEGACY::to_csv_entries)
                .flatMap(Collection::stream)
                .map(Loinc2HpoAnnotationEntryLEGACY::toString)
                .map(String::trim)
                .collect(Collectors.toList());

        BufferedReader reader = new BufferedReader(new FileReader(annotationPath));

        List<String> lines_deserialized = new ArrayList<>();
        String line = reader.readLine();//skip header
        while ((line = reader.readLine()) != null){
            lines_deserialized.add(line.trim());
        }

        assertEquals(lines_to_write.size(), lines_deserialized.size());
        assertArrayEquals(lines_to_write.toArray(), lines_deserialized.toArray());
    }

    @Test
    void getLoincId() {
    }

    @Test
    void getLoincScale() {
    }

    @Test
    void getNote() {
    }

    @Test
    void getFlag() {
    }

    @Test
    void getVersion() {
    }

    @Test
    void setVersion() {
    }

    @Test
    void getCreatedOn() {
    }

    @Test
    void getCreatedBy() {
    }

    @Test
    void getLastEditedOn() {
    }

    @Test
    void getLastEditedBy() {
    }

    @Test
    void whenValueLow() {
    }

    @Test
    void whenValueNormalOrNegative() {
    }

    @Test
    void whenValueHighOrPositive() {
    }

    @Test
    void hasCreatedOn() {
    }

    @Test
    void hasCreatedBy() {
    }

    @Test
    void hasLastEditedOn() {
    }

    @Test
    void hasLastEditedBy() {
    }

    @Test
    void hasComment() {
    }

    @Test
    void getCodes() {
    }

    @Test
    void loincInterpretationToHPO() {
    }

    @Test
    void getCandidateHpoTerms() {
    }

    @Test
    void testToString() throws Exception{
        LoincId loincId = new LoincId("15074-8");
        LoincScale loincScale = LoincScale.string2enum("Qn");
        Term low = hpoTermMap.get("Hypoglycemia");
        Term normal = hpoTermMap.get("Abnormality of blood glucose concentration");
        Term hi = hpoTermMap.get("Hyperglycemia");

        Loinc2HpoAnnotationModelLEGACY glucoseAnnotation = new Loinc2HpoAnnotationModelLEGACY.Builder()
                .setLoincId(loincId)
                .setLoincScale(loincScale)
                .setLowValueHpoTerm(low.getId())
                .setIntermediateValueHpoTerm(normal.getId(), true)
                .setHighValueHpoTerm(hi.getId())
                .build();

        String expected =
                "15074-8\tQn\tFHIR\tL\tHP:0001943\tfalse\tnull\tfalse\t0" +
                        ".0\tNA\tNA\tNA\tNA\n" +
                "15074-8\tQn\tFHIR\tN\tHP:0011015\ttrue\tnull\tfalse\t0.0\tNA\tNA\tNA\tNA\n" +
                "15074-8\tQn\tFHIR\tH\tHP:0003074\tfalse\tnull\tfalse\t0.0\tNA\tNA\tNA\tNA"
                ;
        assertEquals(expected, glucoseAnnotation.toString());



        loincId = new LoincId("600-7");
        loincScale = LoincScale.string2enum("Nom");
        Term forCode1 = hpoTermMap.get("Recurrent E. coli infections");
        Term forCode2 = hpoTermMap.get("Recurrent Staphylococcus aureus infections");
        Term positive = hpoTermMap.get("Recurrent bacterial infections");

        //OutcomeCodeOLD code1 = OutcomeCodeOLD.fromSystemAndCode("http://snomed.info/sct","12283007");
       // OutcomeCodeOLD code2 = OutcomeCodeOLD.fromSystemAndCode("http://snomed.info/sct", "3092008");

        Loinc2HpoAnnotationModelLEGACY bacterialAnnotation;
        Loinc2HpoAnnotationModelLEGACY.Builder bacterialBuilder =
                new Loinc2HpoAnnotationModelLEGACY.Builder();


        //assertEquals(expected, bacterialAnnotation.toString());
    }

 */

}