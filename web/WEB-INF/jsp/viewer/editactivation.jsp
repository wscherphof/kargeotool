<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>
<c:set var="form" value="${activationForm.map}"/>
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
<html:form styleId="activationForm" action="/activation" focus="description">
    <h1><fmt:message key="activation.title"/></h1>
    <table>
        <tr><td></td><td><c:out value="${form.id}"/></td></tr>
        <tr><td></td><td><html:hidden property="location" styleId="thegeom" /></td></tr>
        <tr><td><fmt:message key="activation.commandType"/></td>
            <td>
                <html:select styleId="commandType" property="commandType">
                    <html:option value="0">Aanmeldpunt</html:option>
                    <html:option value="1">Uitmeldpunt</html:option>
                    <html:option value="2">Stopstreep</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="activation.activationGroup"/></td>
            <td><html:select property="activationGroup">
                    <html:option value=""></html:option>
                    <c:forEach items="${activationGroupList}" var="a">
                        <html:option value="${a.id}"><c:out value="${a}"/></html:option>
                    </c:forEach>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="activation.index"/></td>
            <td><html:text property="index"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.validFrom"/></td>
            <td><html:text property="validFrom"/>(dd-mm-yyyy)</td>
        </tr>
        <tr>
            <td><fmt:message key="activation.karUsageType"/></td>
            <td><html:text property="karUsageType"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.type"/></td>
            <td><html:text property="type"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.karDistanceTillStopLine"/></td>
            <td><html:text property="karDistanceTillStopLine"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.karTimeTillStopLine"/></td>
            <td><html:text property="karTimeTillStopLine"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.karRadioPower"/></td>
            <td><html:text property="karRadioPower"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.metersBeforeRoadsSideEquipmentLocation"/></td>
            <td><html:text property="metersBeforeRoadsSideEquipmentLocation"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.angleToNorth"/></td>
            <td><html:text property="angleToNorth"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.updater"/></td>
            <td><html:text property="updater"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.updateTime"/></td>
            <td><html:text property="updateTime"/>(dd-mm-yyyy)</td>
        </tr>
        <tr>
            <td><fmt:message key="activation.validator"/></td>
            <td><html:text property="validator"/></td>
        </tr>
        <tr>
            <td><fmt:message key="activation.validationTime"/></td>
            <td><html:text property="validationTime"/>(dd-mm-yyyy)</td>
        </tr>
        <tr>
            <td></td>
            <td>
                <html:submit property="save">Opslaan</html:submit>
            </td>
        </tr>
    </table>
</html:form>