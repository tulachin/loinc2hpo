package org.monarchinitiative.loinc2hpocore.loinc;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
"LOINC_NUM","COMPONENT","PROPERTY","TIME_ASPCT","SYSTEM","SCALE_TYP","METHOD_TYP","CLASS","CLASSTYPE","LONG_COMMON_NAME","SHORTNAME","EXTERNAL_COPYRIGHT_NOTICE","STATUS","VersionFirstReleased","VersionLastChanged"

 */
public class LoincEntryTest {

    private final static String [] entryFields = {"10000-8","R wave duration.lead AVR","Time","Pt","Heart","Qn","EKG","EKG.MEAS","2","R wave duration in lead AVR","R wave dur L-AVR","","ACTIVE","1.0i","2.48"};
    private final static List<String> quotedEntryFields = Arrays.stream(entryFields).map(w -> String.format("\"%s\"", w)).collect(Collectors.toList());
    private final static String entryLine1 = String.join(",", quotedEntryFields);

    @Test
    void testConstruction() {
        LoincEntry entry = LoincEntry.fromQuotedCsvLine(entryLine1);
        assertEquals("R wave duration.lead AVR", entry.getComponent());
        assertEquals("Pt", entry.getTimeAspect());
        LoincId id = new LoincId("10000-8");
        assertEquals(id, entry.getLoincId());
        assertEquals("EKG",entry.getMethod());
    }


}