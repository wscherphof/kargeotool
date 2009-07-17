/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.kar.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import nl.b3p.kar.persistence.MyEMFDatabase;

/**
 *
 * @author Roy
 */
public class Role {
    public static final String BEHEERDER = "beheerder";
    public static final String GEBRUIKER = "gebruiker";
    
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

    public static Role findByName(String role) throws Exception {
        EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
        Role r = null;
        try {
            r = (Role)em.createQuery("from Role where role = :role")
                    .setParameter("role", role)
                    .getSingleResult();

        } catch(NoResultException nre) {
            // ...
        }
        return r;
    }
}
