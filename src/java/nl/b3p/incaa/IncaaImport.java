/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2013 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.incaa;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import nl.b3p.kar.hibernate.ActivationPoint;
import nl.b3p.kar.hibernate.ActivationPointSignal;
import nl.b3p.kar.hibernate.DataOwner;
import nl.b3p.kar.hibernate.Gebruiker;
import nl.b3p.kar.hibernate.Movement;
import nl.b3p.kar.hibernate.MovementActivationPoint;
import nl.b3p.kar.hibernate.RoadsideEquipment;
import nl.b3p.kar.hibernate.VehicleType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class IncaaImport {
    
    public IncaaImport() {
    }

    private static final int RIJKSDRIEHOEKSTELSEL = 28992;
    
    // Intermediate format: Map<karrAddres, Map<signalGroupNumber, Movement>
    private Map<Integer, Map<Integer, Movement>> movements;
    // Opzoeklijstje obv karaddress
    private Map<Integer, RoadsideEquipment> rseqs;
    // Hou de Points per rseq bij, om later de positie van de rseq te genereren (centroïde)
    private Map<Integer, List<Point>> rseqLocations;


    /**
     * Importeer een reader van een .ptx bestand. 
     * @param in Reader van een INCAA .ptx bestand.
     * @return De lijst van geïmporteerde roadside equipments
     * @throws Exception 
     */
    public List<RoadsideEquipment> importPtx(Reader in, Gebruiker g) throws Exception {
        // (Her)initialiseer de intermediate formats. 
        movements = new HashMap();
        rseqs = new HashMap();
        rseqLocations = new HashMap();
        
        // Definieer een parser en parse de ptx file 
        Iterable<CSVRecord> parser = CSVFormat.newBuilder().withCommentStart('#').withDelimiter('\t').withQuoteChar('"').parse(in);
        
        for (CSVRecord csvRecord : parser) {
            parseRecord(csvRecord);
        }

        // Sla de boel op
        EntityManager em = Stripersist.getEntityManager();
        List<RoadsideEquipment> savedRseqs = new ArrayList();
        for (Integer karAddress : rseqs.keySet()) {
            RoadsideEquipment rseq = rseqs.get(karAddress);
            if(g.canEditDataOwner(rseq.getDataOwner())){
                // Maak van alle punten binnen een rseq een centroïde en gebruik dat als locatie van de rseq zelf
                List<Point> points = rseqLocations.get(rseq.getKarAddress());
                GeometryFactory gf = new GeometryFactory(new PrecisionModel(), RIJKSDRIEHOEKSTELSEL);
                GeometryCollection gc = new GeometryCollection(points.toArray(new Point[points.size()]), gf);
                rseq.setLocation(gc.getCentroid());

                em.persist(rseq);
                savedRseqs.add(rseq);
            }
        }
        em.getTransaction().commit();
        return savedRseqs;
    }

    private void parseRecord(CSVRecord record) {
        ActivationPoint ap = new ActivationPoint();
        EntityManager em = Stripersist.getEntityManager();
        
        // Haal de data uit het csv record
        String beheercode = record.get(0);
        String x = record.get(1);
        String y = record.get(2);
        Integer karAddress = Integer.parseInt(record.get(3));
        Integer signalGroupNumber = Integer.parseInt(record.get(4));
        Integer distanceTillStopline = Integer.parseInt(record.get(5));
        Integer timeTillStopline = Integer.parseInt(record.get(6));
        Integer commandType = Integer.parseInt(record.get(7));

        Integer triggerType = 0;
        try {
            triggerType = Integer.parseInt(record.get(8));// Niet zeker. Mogelijke vertalingen: 0,1: forced, 2,3,4,5: manual
        } catch (IndexOutOfBoundsException ex) {
            // Niet nodig, optioneel veld
        }


        String triggerTypeKar = null;
        switch (triggerType) {
            case 0:
            case 1:
                triggerTypeKar = ActivationPointSignal.TRIGGER_FORCED;
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                triggerTypeKar = ActivationPointSignal.TRIGGER_MANUAL;
                break;
            default:
                throw new IllegalArgumentException("Triggertype not recognized: " + triggerType);
        }

        // Alleen vehicletypes behorende bij de groep hulpdiensten: ptx is voor hulpdiensten
        List<VehicleType> vehicleTypes = em.createQuery("from VehicleType where groep = \'Hulpdiensten\' order by nummer").getResultList();

        // Maak een ActivationPointSignal obv de gegevens uit de ptx
        ActivationPointSignal aps = new ActivationPointSignal();
        aps.setDistanceTillStopLine(distanceTillStopline);
        aps.setSignalGroupNumber(signalGroupNumber);
        aps.setKarCommandType(commandType);
        aps.setTriggerType(triggerTypeKar);
        aps.setVehicleTypes(vehicleTypes);

        // Haal objecten op om aan elkaar te linken
        Movement movement = getMovement(karAddress, signalGroupNumber);
        RoadsideEquipment rseq = movement.getRoadsideEquipment();
        DataOwner dataOwner = getDataOwner(beheercode);

        rseq.setDataOwner(dataOwner);

        // Maak MovementActivationPoint
        MovementActivationPoint map = new MovementActivationPoint();
        map.setPoint(ap);
        map.setMovement(movement);
        map.setBeginEndOrActivation(MovementActivationPoint.ACTIVATION); // geen begin/eindpunt in INCAA
        map.setSignal(aps);

        movement.getPoints().add(map);
        rseq.getMovements().add(movement);

        Set<ActivationPoint> ps = rseq.getPoints();
        ap.setNummer(ps.size() + 1);
        ps.add(ap);

        // Maak geometrie
        int xInt = Integer.parseInt(x);
        int yInt = Integer.parseInt(y);
        ap.setX(xInt);
        ap.setY(yInt);
        
        addPoint(karAddress, ap.getLocation());
        ap.setRoadsideEquipment(rseq);
    }

    /**
     * Haal de movement op behorende bij het karAdress en signalGroupNumber. Binnen een .ptx gaan we ervan uit dat een karAdres 1 rseq 
     * vertegenwoordigd en een punten gegroepeerd op signalGroupNumber een movement vormen.
     * Mocht deze movement nog niet bestaan, dan wordt hij aangemaakt.
     * @param karAddress KarAddress dat binnen dit .ptx bestand een rseq vertegenwoordigd
     * @param signalGroupNumber Signaalgroepnummer dat binnen de rseq van @karAddress een movement identificeert
     * @return De movement binnen een rseq met karAddress @karAddres en met signaalgroepnummer @signalGroupNumber
     */
    private Movement getMovement(Integer karAddress, Integer signalGroupNumber) {
        Movement mvmnt = null;
        RoadsideEquipment rseq = getRseq(karAddress);

        if (!movements.containsKey(karAddress)) {
            Map<Integer, Movement> signalGroupMovement = new HashMap<Integer, Movement>();

            mvmnt = new Movement();
            mvmnt.setRoadsideEquipment(rseq);
            mvmnt.setNummer(signalGroupNumber);
            signalGroupMovement.put(signalGroupNumber, mvmnt);

            movements.put(karAddress, signalGroupMovement);
        } else {
            Map<Integer, Movement> signalGroupMovement = movements.get(karAddress);
            if (!signalGroupMovement.containsKey(signalGroupNumber)) {
                mvmnt = new Movement();
                mvmnt.setRoadsideEquipment(rseq);
                mvmnt.setNummer(signalGroupNumber);
                signalGroupMovement.put(signalGroupNumber, mvmnt);

                movements.put(karAddress, signalGroupMovement);
            }
        }

        mvmnt = movements.get(karAddress).get(signalGroupNumber);
        return mvmnt;
    }

    /**
     * 
     * @param karAddres Haal de rseq op dat aangeduid wordt met een karAddress
     * @return 
     */
    private RoadsideEquipment getRseq(Integer karAddres) {
        RoadsideEquipment rseq = null;

        if (!rseqs.containsKey(karAddres)) {
            rseq = new RoadsideEquipment();
            rseq.setKarAddress(karAddres);
            rseq.setType(RoadsideEquipment.TYPE_CROSSING);
            rseq.setValidFrom(new Date());
            rseqs.put(karAddres, rseq);
        }
        rseq = rseqs.get(karAddres);
        return rseq;
    }

    private DataOwner getDataOwner(String code) {

        EntityManager em = Stripersist.getEntityManager();
        DataOwner dataOwner = (DataOwner) em.createQuery("from DataOwner where code = :code ").setParameter("code", code).getSingleResult();
        return dataOwner;
    }

    /**
     * Voeg een punt toe aan een lijstje voor een rseq met kar adres @karAddress. Voor latere verwerking tot centroïde voor de locatie van het rseq..
     * @param karAddress
     * @param p 
     */
    private void addPoint(Integer karAddress, Point p) {
        if (!rseqLocations.containsKey(karAddress)) {
            rseqLocations.put(karAddress, new ArrayList<Point>());
        }
        rseqLocations.get(karAddress).add(p);
    }
}
