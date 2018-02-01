/**
 * KAR Geo Tool - applicatie voor het registreren van KAR meldpunten
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
import org.stripesstuff.stripersist.Stripersist;

/**
 * Klasse beschrijft een rol en definieert standaard rollen.
 *
 * @author Chris
 */
@Entity
public class Role {
    /**
     * beheerder rol
     */
    public static final String BEHEERDER = "beheerder";
    /**
     * gebruiker rol
     */
    public static final String GEBRUIKER = "gebruiker";

    public static final String VERVOERDER = "vervoerder";

    @Id
    private Integer id;

    private String role;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
}
