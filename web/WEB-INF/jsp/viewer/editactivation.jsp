<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${activationForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="activationForm" staticJavascript="false"/>

<c:if test="${!empty treeUpdate}">
    <script type="text/javascript">
        parent.treeUpdate('${fn:replace(treeUpdate,"'","\\'")}');
    </script>
</c:if>

<c:if test="${!hideForm}">

<html:form styleId="activationForm" action="/activation" onsubmit="return validateActivationForm(this)">
    <html:hidden property="id"/>
    <html:hidden property="agId"/>
    <html:hidden property="location"/>
    <html:submit property="save">Opslaan</html:submit>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" onclick="return confirm('Weet u zeker dat u dit triggerpunt wilt verwijderen?')">Verwijderen</html:submit>
        <input type="button" value="Valideren" onclick="alert('Nog niet geimplementeerd');">
    </c:if>

<c:set var="point" value="${activation.point}" scope="request"/>

<div style="margin-top: 4px; height: 60px">
    <c:set var="prevLocationStatusHtml">
        <c:if test="${empty point}">Nog geen locatie.</c:if>
        <c:if test="${!empty point}">Coordinaten: <span id='location'>${point}</span></c:if>
    </c:set>
    <b>Locatie</b><br>
    <br>
    <span id="locationStatus" style="font-weight: bold">${prevLocationStatusHtml}</span>
    <input id="ok" type="button" style="display: none" value="OK" onclick="okClicked();">
    <input id="cancel" type="button" style="display: none" value="Annuleren" onclick="cancelClick();">
    <input id="newLocation" type="button" ${!empty point ? 'style="display: none"' : ""} value="Locatie aanwijzen in kaart" onclick="newLocationClicked();">
    <input id="changeLocation" type="button" ${empty point ? 'style="display: none"' : ""} value="Locatie wijzigen" onclick="changeLocationClicked();">
    <input id="deleteLocation" type="button" ${empty point ? 'style="display: none"' : ""} value="Wissen" onclick="deleteLocationClicked();">
    <br>
    <c:set var="geometryType" value="PointAtDistance" scope="request"/>
    <c:set var="layer" value="draw_triggerpunten" scope="request"/>
    <tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>
</div>

<div>
    <b>Eigenschappen</b>
    <div class="formTableContainer">
    <table class="form">
        <c:set var="dataOwner" value="${activationGroup.roadsideEquipment.dataOwner}"/>
        <tr>
            <td style="width: 130px">Naam wegbeheerder</td>
            <td class="disabled"><c:out value="${dataOwner.name} (${dataOwner.type})"/></td>
        </tr>
        <tr><td>Nummer walapparaat</td><td class="disabled"><c:out value="${activationGroup.roadsideEquipment.unitNumber}"/></td></tr>
        <tr><td><fmt:message key="a.index"/></td><td class="disabled"><c:out value="${activation.index}"/></td></tr>
        <tr><td><fmt:message key="ag.karSignalGroup"/></td><td class="disabled"><c:out value="${activationGroup.karSignalGroup}"/></td></tr>
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
            <td><fmt:message key="a.triggerType"/></td>
            <td><html:select property="triggerType">
                    <html:option value="PRQA"><fmt:message key="a.triggerType.PRQA"/></html:option>
                    <html:option value="PRQM"><fmt:message key="a.triggerType.PRQM"/></html:option>
                    <html:option value="SDCAS"><fmt:message key="a.triggerType.SDCAS"/></html:option>
                    <html:option value="PRQAA"><fmt:message key="a.triggerType.PRQAA"/></html:option>
                    <html:option value="PRQI"><fmt:message key="a.triggerType.PRQI"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="a.commandType"/><br>
                <fmt:message key="a.commandType_regel2"/>
                </td>
            <td><html:select property="commandType">
                    <html:option value=""/>
                    <html:option value="1"><fmt:message key="a.commandType.In"/></html:option>
                    <html:option value="2"><fmt:message key="a.commandType.Uit"/></html:option>
                    <html:option value="3"><fmt:message key="a.commandType.Voor"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><span id="distanceLabel"><fmt:message key="a.karDistanceTillStopLine"/></span></td>
            <td><html:text style="border: none; width: 98%" property="karDistanceTillStopLine"/></td>
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

</c:if>