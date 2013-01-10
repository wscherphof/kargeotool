package nl.b3p.kar.stripes;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;


/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@StrictBinding
@UrlBinding("/action/viewer/editor")
public class EditorActionBean  implements ActionBean, ValidationErrorHandler {
    
    private static final String JSP = "/WEB-INF/jsp/viewer/editor2.jsp";
    
    private ActionBeanContext context;
    
    @DefaultHandler
    public Resolution view(){
        
        return new ForwardResolution(JSP);
    }
    
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    // <editor-fold desc="Getters and Setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    // </editor-fold>
    
}
