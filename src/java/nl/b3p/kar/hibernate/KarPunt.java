/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.kar.hibernate;

import com.vividsolutions.jts.geom.Point;

/**
 *
 * @author Roy
 */
public class KarPunt {
    private Integer id;
    private Point geom;
    private String description;

    /**
     * @return the code
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param code the code to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the geom
     */
    public Point getGeom() {
        return geom;
    }

    /**
     * @param geom the geom to set
     */
    public void setGeom(Point geom) {
        this.geom = geom;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


}
