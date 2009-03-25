/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.kar.hibernate;

/**
 *
 * @author Roy
 */
public class Role {
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
