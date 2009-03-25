/*
 * $Id: LoginErrorAction.java 9576 2008-11-27 17:38:53Z Matthijs $
 */
package nl.b3p.kar.struts;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.b3p.kar.persistence.MyEMFDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;

public class UserListAction extends Action {
    protected Log log = LogFactory.getLog(this.getClass());

    private final static String DESTINATION = "destination";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);

            request.setAttribute("gebruikers", em.createQuery("from Gebruiker order by id").getResultList());

        } catch (Throwable e) {
            log.error("Exception occured while getting EntityManager: ", e);
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        return mapping.findForward(DESTINATION);
    }
}
