/*
 * $Id: LogoutAction.java 9576 2008-11-27 17:38:53Z Matthijs $
 */

package nl.b3p.kar.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.struts.MessageUtilAction;

import org.apache.struts.action.*;

public class LogoutAction extends MessageUtilAction {

    private final static String DESTINATION = "destination";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.getSession().invalidate();
        addMessage(request, "logout.message");
        return mapping.findForward(DESTINATION);
    }
}
