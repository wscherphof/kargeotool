<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${activationForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="activationForm" staticJavascript="false"/>

<html:form styleId="activationForm" action="/activation" onsubmit="return validateActivationForm(this)">
    <html:hidden property="id"/>
    <html:submit property="save">Opslaan</html:submit>
    <input type="button" value="Verwijderen" onclick="alert('Nog niet geimplementeerd');">
    <input type="button" value="Valideren" onclick="alert('Nog niet geimplementeerd');">

<div style="margin-top: 4px; height: 60px">
    <b>Locatie</b><br>
    <br>
    <c:choose>
        <c:when test="${empty activation.point}">
            <b>Nog geen locatie. </b>
            <input type="button" value="Locatie aanwijzen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
        </c:when>
        <c:otherwise>
            <b>Coordinaten: </b> <span style="font-family: 'courier new', courier, serif"> <c:out value="${activation.point}"/></span>
            <input type="button" value="Locatie wijzigen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
            <%--In de kaart kan het punt worden versleept om de locatie te wijzigen.
            <br><br>
            Op dit punt zijn [nnn]/[geen] andere inmeldpunten aanwezig. Bij het
            wijzigen van de locatie worden deze ook verplaatst.
            --%>
        </c:otherwise>
    </c:choose>
    <br>
</div>

<div>
    <b>Eigenschappen</b><br>
    <br>
    <div class="formTableContainer">
    <table class="form" style="width: 98%" border="1" cellspacing="0" cellpadding="2">
        <c:set var="dataOwner" value="${activation.activationGroup.roadsideEquipment.dataOwner}"/>
        <tr>
            <td style="width: 130px">Naam wegbeheerder</td>
            <td class="disabled"><c:out value="${dataOwner.name} (${dataOwner.type})"/></td>
        </tr>
        <tr><td>Nummer walapparaat</td><td class="disabled"><c:out value="${activation.activationGroup.roadsideEquipment.unitNumber}"/></td></tr>
        <tr><td><fmt:message key="a.index"/></td><td class="disabled"><c:out value="${activation.index}"/></td></tr>
        <tr><td><fmt:message key="ag.karSignalGroup"/></td><td class="disabled"><c:out value="${activation.activationGroup.karSignalGroup}"/></td></tr>
        <tr>
            <td><fmt:message key="a.karUsageType"/></td>
            <td><html:select property="karUsageType">
                    <html:option value="ALL"><fmt:message key="a.karUsageType.ALL"/></html:option>
                    <html:option value="ES"><fmt:message key="a.karUsageType.ES"/></html:option>
                    <html:option value="PT"><fmt:message key="a.karUsageType.PT"/></html:option>
                    <html:option value="ESWA"><fmt:message key="a.karUsageType.ESWA"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="a.type"/></td>
            <td><html:select property="type">
                    <html:option value="PRQA"><fmt:message key="a.type.PRQA"/></html:option>
                    <html:option value="PRQM"><fmt:message key="a.type.PRQM"/></html:option>
                    <html:option value="SDCAS"><fmt:message key="a.type.SDCAS"/></html:option>
                    <html:option value="PRQAA"><fmt:message key="a.type.PRQAA"/></html:option>
                    <html:option value="PRQI"><fmt:message key="a.type.PRQI"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="a.karDistanceTillStopLine"/></td>
            <td><html:text style="border: none; width: 98%" property="karDistanceTillStopLine"/></td>
        </tr>
        <tr>
            <td><fmt:message key="a.metersBeforeRoadsideEquipmentLocation"/></td>
            <td><html:text style="border: none; width: 98%" property="metersBeforeRoadsideEquipmentLocation"/></td>
        </tr>
        <tr>
            <td><fmt:message key="a.karTimeTillStopLine"/></td>
            <td><html:text style="border: none; width: 98%" property="karTimeTillStopLine"/></td>
        </tr>
        <tr>
            <td><fmt:message key="updater"/></td>
            <td class="disabled">
                <c:if test="${!empty activation.updater}">
                    <c:out value="${activation.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activation.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="validator"/></td>
            <td class="disabled">
                <c:if test="${!empty activation.validator}">
                    <c:out value="${activation.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activation.validatorTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>

</div>
</html:form>