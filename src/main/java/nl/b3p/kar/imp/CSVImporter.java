/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.kar.imp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
public class CSVImporter {
    
    private final Log log = LogFactory.getLog(this.getClass()); 
   public CSVImporter(){
        
    }
    
    public List<RoadsideEquipment> process(Reader fr) throws FileNotFoundException{
        List<RoadsideEquipment> rseqs = new ArrayList<>();
        CSVParser p;
        try {
            p = new CSVParser(fr, CSVFormat.DEFAULT);
            Map<String, Integer> header =  p.getHeaderMap();
            
            if(validateHeader(header)){
                // verzamel zakje karpunten per rseq
                // per rseq: groepeer movements
                // per movement movementactivationpoints maken
                List<List<CSVRecord>> rseqsList = pointsPerRseq(p);
                for (List<CSVRecord> r : rseqsList) {
                    RoadsideEquipment rseq = processRseq(r);
                    rseqs.add(rseq);
                }
            }
                
        } catch (IOException ex) {
            log.error("Cannot parse csv file",ex);
        }
        return rseqs;
    }
    
    private RoadsideEquipment processRseq(List<CSVRecord> records){
        RoadsideEquipment rseq = new RoadsideEquipment();
        return rseq;
        
    }
    
    private List<List<CSVRecord>> pointsPerRseq(CSVParser p) throws IOException{
        Map<String, List<CSVRecord>> pointsByRseq = new HashMap<>();
        List<CSVRecord> records = p.getRecords();
        for (CSVRecord record : records) {
            String id = getRseqIdentifier(record);
            
            if(!pointsByRseq.containsKey(id)){
                pointsByRseq.put(id, new ArrayList<CSVRecord>());
            }
            pointsByRseq.get(id).add(record);
        }
        
        return new ArrayList<>(pointsByRseq.values());
    }
    
    private String getRseqIdentifier(CSVRecord c){
       String soort = c.get(0);
       String beheerder = c.get(1);
       String aanduiding = c.get(2);
       String plaats = c.get(3);
       String karadres = c.get(7);
       return soort + beheerder + aanduiding + plaats + karadres;
    }
    
    private boolean validateHeader(Map<String, Integer> header){
        return true;
    }
    
}
