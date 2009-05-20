<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${activationForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="activationGroupForm" staticJavascript="false"/>

<html:form styleId="activationGroupForm" action="/activationGroup" onsubmit="return validateActivationGroupForm(this)">
    <html:hidden property="id"/>
    <html:hidden property="rseqId"/>
    <html:submit property="save">Opslaan</html:submit>
    <input type="button" value="Verwijderen" onclick="alert('Nog niet geimplementeerd');">
    <input type="button" value="Valideren" onclick="alert('Nog niet geimplementeerd');">

<div style="margin-top: 4px; height: 60px">
    <b>Locatie</b><br>
    <br>
    <c:choose>
        <c:when test="${empty activationGroup.point}">
            <b>Nog geen locatie. </b>
            <input type="button" value="Locatie aanwijzen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
        </c:when>
        <c:otherwise>
            <b>Coordinaten: </b> <span style="font-family: 'courier new', courier, serif"> <c:out value="${activationGroup.point}"/></span>
            <input type="button" value="Locatie wijzigen in kaart" onclick="alert('Nog niet geimplementeerd');">
            <br>
            <%--
            In de kaart kan het punt worden versleept om de locatie te wijzigen.
            <br><br>
            Op dit punt zijn [nnn]/[geen] andere signaalgroepen aanwezig. Bij het
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
        <tr>
            <td style="width: 130px">Naam wegbeheerder</td>
            <td class="disabled"><c:out value="${rseq.dataOwner.name} (${rseq.dataOwner.type})"/></td>
        </tr>
        <tr><td>Nummer walapparaat</td><td class="disabled"><c:out value="${rseq.unitNumber}"/></td></tr>
        <tr>
            <td><fmt:message key="ag.karSignalGroup"/></td>
            <td>
                <html:text property="karSignalGroup" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><fmt:message key="ag.directionAtIntersection"/></td>
            <td>
<script type="text/javascript">
    function onbekendChanged() {
        if(document.getElementById("onbekend").checked) {
            document.getElementById("rechtdoor").checked = false;
            document.getElementById("linksaf").checked = false;
            document.getElementById("rechtsaf").checked = false;
        } else {
            if(!(document.getElementById("rechtdoor").checked
            || document.getElementById("linksaf").checked
            || document.getElementById("rechtsaf").checked)) {
                document.getElementById("onbekend").checked = true;
            }
        }
    }

    function richtingChanged() {
        if(document.getElementById("rechtdoor").checked
        || document.getElementById("linksaf").checked
        || document.getElementById("rechtsaf").checked) {
            document.getElementById("onbekend").checked = false;
        } else {
            document.getElementById("onbekend").checked = true;
        }
    }
</script>
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td style="vertical-align: top"><html:multibox styleId="onbekend" property="directionAtIntersection" value="onbekend" onclick="onbekendChanged()"/></td>
                        <td>
                            <label for="onbekend">
                                Onbekend (complexe kruising, handmatig bepalen)
                            </label>
                        </td>
                    </tr>
                    <tr>
                        <td style="vertical-align: top"><html:multibox styleId="rechtdoor" property="directionAtIntersection" value="rechtdoor" onclick="richtingChanged()"/></td>
                        <td><label for="rechtdoor">Rechtdoor</label></td>
                    </tr>
                    <tr>
                        <td style="vertical-align: top"><html:multibox styleId="linksaf" property="directionAtIntersection" value="linksaf" onclick="richtingChanged()"/></td>
                        <td><label for="linksaf">Linksaf</label></td>
                    </tr>
                    <tr>
                        <td style="vertical-align: top"><html:multibox styleId="rechtsaf" property="directionAtIntersection" value="rechtsaf" onclick="richtingChanged()"/></td>
                        <td><label for="rechtsaf">Rechtsaf</label></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="ag.metersAfterRoadsideEquipmentLocation"/>
            <span style="font-size: 8pt"><br>meters na stopstreep</span>
            </td>
            <td>
                <html:text property="metersAfterRoadsideEquipmentLocation" style="border: none; width: 98%"/>
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
            <td>
                <html:textarea rows="2" style="width: 100%; border: none; font-family: tahoma, sans-serif; font-size: 8pt" property="description"/>
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
    </div>
</div>

</html:form>