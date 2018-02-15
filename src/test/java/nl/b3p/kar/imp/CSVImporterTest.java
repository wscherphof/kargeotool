/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.csv.CSVRecord;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author Meine Toonen
 */
public class CSVImporterTest {
    
    
    private CSVImporter instance;
    private CSVRecord firstPoint;
    
    @Before
    public void init() throws IOException{
        InputStream is =this.getClass().getResourceAsStream("test.csv");
        Reader reader = new InputStreamReader(is);
        List<DataOwner> das = new ArrayList<>();
        DataOwner da = new DataOwner();
        da.setOmschrijving("B3Partners");
        da.setId(666);
        das.add(da);
                
        instance = new CSVImporter(reader,das);
        instance.init();
    }
    
    @After
    public void shutdown() throws IOException{
        instance.close();
    }
    
    public CSVImporterTest() {
    }

  //  @Test
    public void testProcess() throws Exception {
        
    }

    @Test
    public void testProcessRseq() throws Exception {
        List<List<CSVRecord>> recordsPerRseq = instance.pointsPerRseq();
        for (List<CSVRecord> r : recordsPerRseq) {
            RoadsideEquipment rseq = instance.processRseq(r);
            assertEquals(2, rseq.getMovements().size());
            assertEquals(10, rseq.getPoints().size());
        }
    }

   // @Test
    public void testProcessMovement() {
    }
    
    @Test
    public void testCreateRseq() throws IOException, ParseException{
        List<List<CSVRecord>> recordsPerRseq = instance.pointsPerRseq();
        List<CSVRecord> rseqRecord = recordsPerRseq.get(0);
        RoadsideEquipment r = instance.parseRseq(rseqRecord);
        assertEquals(RoadsideEquipment.TYPE_GUARD,r.getType());
        assertEquals("B3Partners" ,r.getDataOwner().getOmschrijving());
        assertEquals("werwer" ,r.getDescription());
        assertEquals("Gouda" ,r.getTown());
        assertEquals("Gemengd" ,r.getCrossingCode());
        assertEquals(new Integer(666),r.getKarAddress());
        
        DateFormat stomdateformat = new SimpleDateFormat("dd-MM-yyyy");
        assertEquals("08-02-2018" ,stomdateformat.format(r.getValidFrom()));
        assertEquals(null,r.getValidUntil());
    }

    @Test
    public void testPointsByMovement() throws Exception {
        List<List<CSVRecord>> recordsPerRseq = instance.pointsPerRseq();
        for (List<CSVRecord> list : recordsPerRseq) {
            List<List<CSVRecord>> movements = instance.recordsByMovement(list);
            assertEquals(2, movements.size());
            for (List<CSVRecord> movement : movements) {
                assertEquals(5, movement.size());
            }
        }
    }

    @Test
    public void testPointsPerRseq() throws Exception {
        List<List<CSVRecord>> rs = instance.pointsPerRseq();
        assertEquals(1, rs.size());
    }

}
