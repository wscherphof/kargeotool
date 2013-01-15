package nl.b3p.kar.hibernate;

import javax.persistence.*;

/**
 * Entity voor KAR soort voertuig uit BISON koppelvlak 9. Zie bestand 
 * "database/05 insert vehicle types.sql".
 * 
 * @author Matthijs Laan
 */
@Entity
public class VehicleType {
    
    /**
     * Waarde voor in Kv9 XML; KARb1 bericht attribuut 2.
     */
    @Id
    private int nummer;
    
    /**
     * Omschrijving van het soort voertuig.
     */
    private String omschrijving;

    public int getNummer() {
        return nummer;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }
    
}
