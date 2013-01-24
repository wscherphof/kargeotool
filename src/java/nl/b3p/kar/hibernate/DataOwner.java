/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten               
 *                                                                           
 * Copyright (C) 2009-2013 B3Partners B.V.                                   
 *                                                                           
 * This program is free software: you can redistribute it and/or modify      
 * it under the terms of the GNU Affero General Public License as            
 * published by the Free Software Foundation, either version 3 of the        
 * License, or (at your option) any later version.                           
 *                                                                           
 * This program is distributed in the hope that it will be useful,           
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              
 * GNU Affero General Public License for more details.                       
 *                                                                           
 * You should have received a copy of the GNU Affero General Public License  
 * along with this program. If not, see <http://www.gnu.org/licenses/>.      
 */

package nl.b3p.kar.hibernate;

import javax.persistence.*;

/**
 * Entity voor een data eigenaar uit BISON koppelvlak 9.
 * 
 * @author Matthijs Laan
 */
@Entity
public class DataOwner {
    /**
     * definitie voor vervoerder
     */
    public static final String CLASSIFICATIE_VERVOERDER = "Vervoerder";
    /**
     * definitie voor integrator
     */
    public static final String CLASSIFICATIE_INTEGRATOR = "Integrator";
    /**
     * definitie voor infra beheerder
     */
    public static final String CLASSIFICATIE_INFRA_BEHEERDER = "Infra Beheerder";
    
    @Id
    private String code;
    
    private Integer companyNumber;
    
    private String omschrijving;
    
    private String classificatie;

    //<editor-fold defaultstate="collapsed" desc="getters en setters">
    /**
     *
     * @return
     */
    public String getCode() {
        return code;
    }
    
    /**
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     *
     * @return companyNumber
     */
    public Integer getCompanyNumber() {
        return companyNumber;
    }
    
    /**
     *
     * @param companyNumber
     */
    public void setCompanyNumber(Integer companyNumber) {
        this.companyNumber = companyNumber;
    }
    
    /**
     *
     * @return omschrijving
     */
    public String getOmschrijving() {
        return omschrijving;
    }
    
    /**
     *
     * @param omschrijving
     */
    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }
    
    /**
     * 
     * @return classificatie
     */
    public String getClassificatie() {
        return classificatie;
    }
    
    /**
     *
     * @param classificatie
     */
    public void setClassificatie(String classificatie) {
        this.classificatie = classificatie;
    }
    //</editor-fold>
    
}
