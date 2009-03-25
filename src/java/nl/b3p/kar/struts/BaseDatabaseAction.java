package nl.b3p.kar.struts;

import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.struts.ExtendedMethodAction;
import nl.b3p.kar.persistence.MyEMFDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public abstract class BaseDatabaseAction extends ExtendedMethodAction {

    private static final Log log = LogFactory.getLog(BaseDatabaseAction.class);
    protected static final String SUCCESS = "success";
    protected static final String FAILURE = "failure";
    protected static final String VALIDATION_ERROR_KEY = "error.validation";
    protected static final String RIGHTS_ERROR_KEY = "error.rights";
    public static final int WRITE = 0;
    public static final int READ = 1;
    public static final int OPENBAAR = 1;
    public static final int AFGESCHERMD = 2;
    public static final int PRIVE = 3;

    /**
     * Een Struts action die doorverwijst naar de strandaard forward.
     *
     * @param mapping ActionMapping die gebruikt wordt voor deze forward.
     * @param request HttpServletRequest die gebruikt wordt voor deze forward.
     *
     * @return ActionForward met de struts forward waar deze methode naar toe moet verwijzen.
     *
     */
    // <editor-fold defaultstate="" desc="protected ActionForward getUnspecifiedDefaultForward(ActionMapping mapping, HttpServletRequest request)">
    protected ActionForward getUnspecifiedDefaultForward(ActionMapping mapping, HttpServletRequest request) {
        return mapping.findForward(SUCCESS);
    }
    // </editor-fold>

    /**
     * Een Struts action execute die verwijst naar de standaard action als alles goed verloopt en anders een
     * alternatieve forward aanroept.
     *
     * @param mapping ActionMapping die gebruikt wordt voor deze forward.
     * @param form ActionForm die gebruikt wordt voor deze forward.
     * @param request HttpServletRequest die gebruikt wordt voor deze forward.
     * @param response HttpServletResponse die gebruikt wordt voor deze forward.
     *
     * @return ActionForward met de struts forward waar deze methode naar toe moet verwijzen.
     *
     * @throws Exception
     *
     */
    // <editor-fold defaultstate="" desc="public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception">
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Object identity = null;
        try {
            identity = MyEMFDatabase.createEntityManager(MyEMFDatabase.MAIN_EM);
            EntityManager em = getEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            ActionForward forward = null;
            String msg = null;
            try {
                forward = super.execute(mapping, form, request, response);
                tx.commit();
                return forward;
            } catch (Exception e) {
                tx.rollback();
                log.error("Exception occured, rollback", e);

                if (e instanceof org.hibernate.JDBCException) {
                    msg = e.getMessage();
                    SQLException sqle = ((org.hibernate.JDBCException) e).getSQLException();
                    msg = msg + ": " + sqle;
                    SQLException nextSqlE = sqle.getNextException();
                    if (nextSqlE != null) {
                        msg = msg + ": " + nextSqlE;
                    }
                } else if (e instanceof java.sql.SQLException) {
                    msg = e.getMessage();
                    SQLException nextSqlE = ((java.sql.SQLException) e).getNextException();
                    if (nextSqlE != null) {
                        msg = msg + ": " + nextSqlE;
                    }
                } else {
                    msg = "Exception " + e.getClass().getName() + ": " + e.getMessage();
                }
                addAlternateMessage(mapping, request, null, msg);
            }
        } catch (Throwable e) {
            log.error("Exception occured while getting EntityManager: ", e);
            addAlternateMessage(mapping, request, null, e.toString());
        } finally {
            log.debug("Closing entity manager .....");
            MyEMFDatabase.closeEntityManager(identity, MyEMFDatabase.MAIN_EM);
        }
        return getAlternateForward(mapping, request);
    }

    protected static EntityManager getEntityManager() throws Exception {
        log.debug("Getting entity manager ......");
        return MyEMFDatabase.getEntityManager(MyEMFDatabase.MAIN_EM);
    }
   
}