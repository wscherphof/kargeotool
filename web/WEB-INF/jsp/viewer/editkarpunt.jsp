<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>
<c:set var="form" value="${karpuntForm.map}"/>
<script type="text/javascript">
    //temp oplossing!!!!!! Dit moet nog worden veranderd
    if ("${form.description}".length !=0){
        if (window.opener){
            window.opener.removeDrawing();
            window.opener.refreshFlamingo();
        }
        window.close();
    }
</script>
<html:form styleId="karpuntForm" action="/karpunt" focus="description">
    <h1>Karpunt opslaan</h1>
    <table>
        <tr><td></td><td><c:out value="${form.id}"/></td></tr>
        <tr><td></td><td><html:hidden property="thegeom" styleId="thegeom" /></td></tr>
        <tr><td>Omschrijving</td>
            <td>
                <html:select styleId="description" property="description">
                    <html:option value="Aanmeldpunt">Aanmeldpunt</html:option>
                    <html:option value="Uitmeldpunt">Uitmeldpunt</html:option>
                    <html:option value="Stopstreep">Stopstreep</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <html:submit property="save">Opslaan</html:submit>
            </td>
        </tr>
    </table>
</html:form>