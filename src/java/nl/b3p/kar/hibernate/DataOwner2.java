package nl.b3p.kar.hibernate;

import javax.persistence.*;

/**
 * Entity voor een data eigenaar uit BISON koppelvlak 9.
 * 
 * @author Matthijs Laan
 */
@Entity
public class DataOwner2 {
    public static final String CLASSIFICATIE_VERVOERDER = "Vervoerder";
    public static final String CLASSIFICATIE_INTEGRATOR = "Integrator";
    public static final String CLASSIFICATIE_INFRA_BEHEERDER = "Infra Beheerder";
    
    @Id
    private String code;
    
    private Integer companyNumber;
    
    private String omschrijving;
    
    private String classificatie;

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Integer getCompanyNumber() {
        return companyNumber;
    }
    
    public void setCompanyNumber(Integer companyNumber) {
        this.companyNumber = companyNumber;
    }
    
    public String getOmschrijving() {
        return omschrijving;
    }
    
    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }
    
    public String getClassificatie() {
        return classificatie;
    }
    
    public void setClassificatie(String classificatie) {
        this.classificatie = classificatie;
    }
    //</editor-fold>
    
}
