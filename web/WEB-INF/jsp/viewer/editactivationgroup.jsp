<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${activationForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="activationGroupForm" staticJavascript="false"/>

<html:form styleId="activationGroupForm" action="/activationGroup" onsubmit="return validateActivationGroupForm(this)">
    <html:hidden property="id"/>
    <html:submit property="save">Opslaan</html:submit>
    <input type="button" value="Verwijderen" onclick="alert('Nog niet geimplementeerd');">
    <input type="button" value="Valideren" onclick="alert('Nog niet geimplementeerd');">
    <table cellpadding="2">
        <tr><td>
            <b>Eigenschappen</b><br>

    <table class="form" border="1" cellspacing="0" cellpadding="1">
        <c:set var="dataOwner" value="${activationGroup.roadsideEquipment.dataOwner}"/>
        <tr>
            <td style="width: 180px">Naam wegbeheerder</td>
            <td style="width: 300px" class="number disabled"><c:out value="${dataOwner.name} (${dataOwner.type})"/></td>
        </tr>
        <tr><td>Nummer walapparaat</td><td class="number disabled"><c:out value="${activationGroup.roadsideEquipment.unitNumber}"/></td></tr>
        <tr>
            <td><fmt:message key="ag.karSignalGroup"/></td>
            <td class="number">
                <html:text property="karSignalGroup" style="border: none; text-align: right; width: 100%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="ag.directionAtIntersection"/></td>
            <td>
<script type="text/javascript">
    function clearAnderen() {
        document.getElementById("rechtdoor").checked = false;
        document.getElementById("linksaf").checked = false;
        document.getElementById("rechtsaf").checked = false;
    }

    function clearOnbekend() {
        document.getElementById("onbekend").checked = false;
    }
</script>
                <label><html:multibox styleId="onbekend" property="directionAtIntersection" value="onbekend" onchange="clearAnderen()"/>Onbekend</label>
                <span style="font-size: 8pt">(complexe kruising,<br>handmatig bepalen)</span><br>
                <label><html:multibox styleId="rechtdoor" property="directionAtIntersection" value="rechtdoor" onchange="clearOnbekend()"/>Rechtdoor</label><br>
                <label><html:multibox styleId="linksaf" property="directionAtIntersection" value="linksaf" onchange="clearOnbekend()"/>Linksaf</label><br>
                <label><html:multibox styleId="rechtsaf" property="directionAtIntersection" value="rechtsaf" onchange="clearOnbekend()"/>Rechtsaf</label><br>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="ag.metersAfterRoadsideEquipmentLocation"/>
            <span style="font-size: 8pt"><br>meters na stopstreep</span>
            </td>
            <td class="number">
                <html:text property="metersAfterRoadsideEquipmentLocation" style="border: none; text-align: right; width: 100%"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="ag.followDirection"/></td>
            <td>
                <html:select property="followDirection">
                    <html:option value="false">Nee</html:option>
                    <html:option value="true">Ja</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="ag.description"/></td>
            <td class="number">
                <html:textarea cols="25" rows="5" style="border: none; font: arial, helvetica, sans-serif; font-size: 8pt" property="description"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="updater"/></td>
            <td class="disabled">
                <c:if test="${!empty activationGroup.updater}">
                    <c:out value="${activationGroup.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="validator"/></td>
            <td class="disabled">
                <c:if test="${!empty activationGroup.validator}">
                    <c:out value="${activationGroup.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.validatorTime}"/>
                </c:if>
            </td>
        </tr>
    </table>

        </td>
        <td style="vertical-align: top; width: 50%">

<b>Locatie</b><br>
<br>
<c:choose>
    <c:when test="${empty activationGroup.point}">
        <b>Nog geen locatie. </b>
        <input type="button" value="Locatie aanwijzen in kaart" onclick="alert('Nog niet geimplementeerd');">
    </c:when>
    <c:otherwise>
<b>Coordinaten: </b> <span style="font-family: 'courier new', courier, serif"> <c:out value="${activationGroup.point}"/></span><br>
<br>
<%--
In de kaart kan het punt worden versleept om de locatie te wijzigen.
<br><br>
Op dit punt zijn [nnn]/[geen] andere signaalgroepen aanwezig. Bij het
wijzigen van de locatie worden deze ook verplaatst.
--%>
    </c:otherwise>
</c:choose>
        </td></tr>
    </table>
</html:form>