<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${activationGroupForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="activationGroupForm" staticJavascript="false"/>

<c:if test="${!empty treeUpdate}">
    <script type="text/javascript">
        parent.treeUpdate('${fn:replace(treeUpdate,"'","\\'")}');
    </script>
</c:if>

<script type="text/javascript">
    parent.cancelEdit();

    var agIds = <%= request.getAttribute("ActivationGroupIds")%>;
    var aIds = <%= request.getAttribute("ActivationIds")%>;
    var rseqId = <%= request.getAttribute("RoadSideEquipmentId")%>;

    parent.showSelected( rseqId, agIds, aIds);
    parent.oc.update();

    <c:if test="${notEditable}">
        parent.notEditable();
    </c:if>
</script>

<c:if test="${!hideForm}">
    
<html:form disabled="${notEditable}" styleId="activationGroupForm" action="/activationGroup" onsubmit="return validateActivationGroupForm(this)">
    <%-- geen struts tag omdat form tag disabled="true" kan zijn... maar validate submit mogelijk wel mogelijk --%>
    <input type="hidden" name="id" value="${form.id}">
    <input type="hidden" name="validated">
    <html:hidden property="copyFrom"/>
    <html:hidden property="rseqId"/>
    <html:hidden property="location"/>
    <html:submit property="save" title="Sla de wijzigingen steeds op per walapparaat, signaalgroep of triggerpunt.">Opslaan</html:submit>
    <c:if test="${activationCount == 1}">
        <c:set var="extraMsg">Let op! Het triggerpunt van deze signaalgroep wordt ook verwijderd!</c:set>
    </c:if>
    <c:if test="${activationCount > 1}">
        <c:set var="extraMsg">Let op! De ${activationCount} triggerpunten van deze signaalgroep worden ook verwijderd!</c:set>
    </c:if>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" title="De signaalgroep en de bijbehorende triggerpunten verwijderen." onclick="bCancel = true; return confirm('Weet u zeker dat u deze signaalgroep wilt verwijderen? ${extraMsg}')">Verwijderen</html:submit>
        <%-- geen struts tag omdat form tag disabled="true" kan zijn... --%>
        <input type="submit" name="validate" title="De data van de signaalgroepen en de bijbehorende triggerpunten valideren." ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');" value="Valideren">
        <html:submit property="copy" title="Selecteer deze optie voor het kopiëren van de signaalgroep inclusief de bijbehorende triggerpunten. Op deze manier kunnen eenvoudig signaalgroepen en triggerpunten die op dezelfde kruispuntarm liggen worden aangemaakt. Maak hiervoor eerst één van de signaalgroepen op een kruispuntarm aan, inclusief de bijbehorende triggerpunten. Na het kopiëren van deze signaalgroep dienen alleen de gegevens en de locatie van de nieuwe signaalgroep aangepast te worden. De nieuwe triggerpunten worden automatisch gekoppeld aan de nieuwe signaalgroep.">Kopi&euml;ren</html:submit>
    </c:if>
<c:set var="point" value="${activationGroup.stopLineLocationString}" scope="request"/>
<c:set var="geometryType" value="Point" scope="request"/>
<tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>

<c:set var="focus" value="karSignalGroup" scope="request"/>
<tiles:insert definition="setFocus"/>

<div>
    <b>Eigenschappen</b>
    <div class="formTableContainer">
    <table class="form">
        <tr>
            <td style="width: 130px"><label title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data.">Databeheerder</label></td>
            <td class="disabled" title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data."><c:out value="${rseq.dataOwner.name}"/></td>
        </tr>
        <tr><td><label title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer).">Nummer walapparaat</label></td><td class="disabled" title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)."><c:out value="${rseq.unitNumber}"/></td></tr>
        <tr>
            <td><label title="Voer hier de signaalgroep in waarvoor u in- en uitmeldpunten wilt aanmaken."><fmt:message key="ag.karSignalGroup"/></label></td>
            <td>
                <html:text title="Voer hier de signaalgroep in waarvoor u in- en uitmeldpunten wilt aanmaken." property="karSignalGroup" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="Selecteer de rijrichtingen die mogelijk zijn bij de signaalgroep."><fmt:message key="ag.directionAtIntersection"/></label></td>
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
                <table cellspacing="0" cellpadding="0" title="Selecteer de rijrichtingen die mogelijk zijn bij de signaalgroep.">
                    <tr>
                        <td style="vertical-align: top"><html:multibox styleId="rechtsaf" property="directionAtIntersection" value="rechtsaf" onclick="richtingChanged()"/></td>
                        <td><label for="rechtsaf">Rechtsaf</label></td>
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
                        <td style="vertical-align: top"><html:multibox styleId="onbekend" property="directionAtIntersection" value="onbekend" onclick="onbekendChanged()"/></td>
                        <td>
                            <label for="onbekend">
                                Richting ongedefinieerd (complexe kruising, handmatig bepalen)
                            </label>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
                <td><label title="Indien het uitmeldpunt op de stopstreep ligt, wordt geen afzonderlijk uitmeldpunt vastgelegd."><fmt:message key="ag.leaveAnnouncement"/></label></td>
            <td>
                <html:select property="leaveAnnouncement" title="Indien het uitmeldpunt op de stopstreep ligt, wordt geen afzonderlijk uitmeldpunt vastgelegd.">
                    <html:option value="false">Nee</html:option>
                    <html:option value="true">Ja</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Indien de signaalgroep via een koppelsignaal gekoppeld is aan bijv. een uitmelding van een eerdere signaalgroep, is het niet nodig een inmeldpunt te definiëren voor deze signaalgroep. Een uitmeldpunt wordt alleen vastgelegd indien een vaste nalooptijd ontbreekt."><fmt:message key="ag.followDirection"/></label></td>
            <td>
                <html:select property="followDirection" title="Indien de signaalgroep via een koppelsignaal gekoppeld is aan bijv. een uitmelding van een eerdere signaalgroep, is het niet nodig een inmeldpunt te definiëren voor deze signaalgroep. Een uitmeldpunt wordt alleen vastgelegd indien een vaste nalooptijd ontbreekt.">
                    <html:option value="false">Nee</html:option>
                    <html:option value="true">Ja</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="Ruimte voor opmerkingen. Indien de signaalgroep een volgrichting is kan worden aangeven van welke richting het een volgrichting is."><fmt:message key="ag.description"/></label></td>
            <td>
                <html:textarea title="Ruimte voor opmerkingen. Indien de signaalgroep een volgrichting is kan worden aangeven van welke richting het een volgrichting is." rows="2" style="width: 100%; border: none; font-family: tahoma, sans-serif; font-size: 8pt" property="description"/>
            </td>
        </tr>
        <tr>
            <td><label title="Datum waarop de signaalgroep volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj"><fmt:message key="rseq.inactiveFrom"/></label></td>
            <td class="disabled" title="Datum waarop de signaalgroep volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj">
                <c:if test="${!empty activationGroup.inactiveFrom}">
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.inactiveFrom}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><label title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum."><fmt:message key="updater"/></label></td>
            <td class="disabled" title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum.">
                <c:if test="${!empty activationGroup.updater}">
                    <c:out value="${activationGroup.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><label title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum."><fmt:message key="validator"/></label></td>
            <td class="disabled" title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum.">
                <c:if test="${!empty activationGroup.validator}">
                    <html:img page="/images/checkmark.gif" module="" style="display: block; float: left;"/>
                    <c:out value="${activationGroup.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.validationTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>
</div>

</html:form>

</c:if>