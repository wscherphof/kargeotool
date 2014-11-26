/**
 * Geo-OV - applicatie voor het registreren van KAR meldpunten
 *
 * Copyright (C) 2009-2014 B3Partners B.V.
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
package nl.b3p.kar.hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author Meine Toonen
 */
@Entity
public class InformMessage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional=false)
    private Gebruiker vervoerder;

    @ManyToOne(optional=false)
    private Gebruiker afzender;

    @ManyToOne(optional=false)
    private RoadsideEquipment rseq;


    // <editor-fold defaultstate="collapsed" desc="Getters and setters">
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Gebruiker getVervoerder() {
        return vervoerder;
    }

    public void setVervoerder(Gebruiker vervoerder) {
        this.vervoerder = vervoerder;
    }

    public Gebruiker getAfzender() {
        return afzender;
    }

    public void setAfzender(Gebruiker afzender) {
        this.afzender = afzender;
    }

    public RoadsideEquipment getRseq() {
        return rseq;
    }

    public void setRseq(RoadsideEquipment rseq) {
        this.rseq = rseq;
    }


    // </editor-fold>
}
