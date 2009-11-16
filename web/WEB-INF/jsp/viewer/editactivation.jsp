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

<script type="text/javascript">
    parent.flamingo_cancelEdit();

    var agIds = <%= request.getAttribute("ActivationGroupIds")%>;
    var aIds = <%= request.getAttribute("ActivationIds")%>;
    var rseqId = <%= request.getAttribute("RoadSideEquipmentId")%>;
    parent.showSelected( rseqId, agIds, aIds);
    parent.flamingo_updateKarLayer();

    <c:if test="${notEditable}">
        parent.notEditable();
    </c:if>
</script>

<c:if test="${!hideForm}">

<html:form disabled="${notEditable}" styleId="activationForm" action="/activation" onsubmit="return validateActivationForm(this)">
    <%-- geen struts tag omdat form tag disabled="true" kan zijn... maar validate submit mogelijk wel mogelijk --%>
    <input type="hidden" name="id" value="${form.id}">
    <input type="hidden" name="validated">
    <html:hidden property="agId"/>
    <html:hidden property="location"/>
    <html:submit property="save" title="Sla de wijzigingen steeds op per walapparaat, signaalgroep of triggerpunt.">Opslaan</html:submit>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" title="Het triggerpunt verwijderen." onclick="bCancel = true; return confirm('Weet u zeker dat u dit triggerpunt wilt verwijderen?')">Verwijderen</html:submit>
        <%-- geen struts tag omdat form tag disabled="true" kan zijn... --%>
        <input type="submit" name="validate" title="De data van het triggerpunt valideren." ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');" value="Valideren">
        <html:submit property="copy" title="Selecteer deze optie voor het kopiëren van het triggerpunt. Op deze manier kunnen eenvoudig meerdere triggerpunten met bijvoorbeeld dezelfde eigenschappen, maar met verschillende locaties, aan de signaalgroep worden toegevoegd.">Kopi&euml;ren</html:submit>
    </c:if>

<c:set var="point" value="${activation.locationString}" scope="request"/>
<c:set var="geometryType" value="PointAtDistance" scope="request"/>
<c:set var="layer" value="draw_triggerpunten" scope="request"/>
<tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>

<c:set var="focus" value="commandType" scope="request"/>
<tiles:insert definition="setFocus"/>

<div>
    <b>Eigenschappen</b>
    <div class="formTableContainer">
    <table class="form">
        <c:set var="dataOwner" value="${activationGroup.roadsideEquipment.dataOwner}"/>
        <tr>
            <td style="width: 130px"><label title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data.">Databeheerder</label></td>
            <td class="disabled" title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data."><c:out value="${dataOwner.name}"/></td>
        </tr>
        <tr><td><label title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer).">Nummer walapparaat</label></td><td class="disabled" title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)."><c:out value="${activationGroup.roadsideEquipment.unitNumber}"/></td></tr>
        <tr><td><label title="Binnen elke signaalgroep krijgen de triggerpunten een volgnummer toegewezen."><fmt:message key="a.index"/></label></td><td title="Binnen elke signaalgroep krijgen de triggerpunten een volgnummer toegewezen." class="disabled"><c:out value="${activation.index}"/></td></tr>
        <tr><td><label title="De signaalgroep waar het triggerpunt bij hoort."><fmt:message key="ag.karSignalGroup"/></label></td><td class="disabled" title="De signaalgroep waar het triggerpunt bij hoort."><c:out value="${activationGroup.karSignalGroup}"/></td></tr>
        <tr>
            <td><label title="Voor welke voertuigcategorie is het triggerpunt bedoeld?"><fmt:message key="a.karUsageType"/></label></td>
            <td><html:select property="karUsageType" style="width: 98%" title="Voor welke voertuigcategorie is het triggerpunt bedoeld?">
                    <html:option value="ES"><fmt:message key="a.karUsageType.ES"/></html:option>
                    <html:option value="PT"><fmt:message key="a.karUsageType.PT"/></html:option>
                    <html:option value="ESWA"><fmt:message key="a.karUsageType.ESWA"/></html:option>
                    <html:option value="DS"><fmt:message key="a.karUsageType.DS"/></html:option>
                    <html:option value="ESPT"><fmt:message key="a.karUsageType.ESPT"/></html:option>
                    <html:option value="ESWAPT"><fmt:message key="a.karUsageType.ESWAPT"/></html:option>
                    <html:option value="ALL"><fmt:message key="a.karUsageType.ALL"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Selecteer hier op welke wijze de in- of uitmelding geactiveerd kan worden.Automatisch: De in- of uitmelding wordt automatisch geactiveerd bij passage van het triggerpunt, Start Deur Sluiten: Indien zich vlak voor het kruispunt/de wegafsluiting een halte bevindt, kan het deur sluiten moment van de bus  worden gebruikt om het KAR bericht te versturen. Advies is het inmeldpunt ongeveer 75 meter (zichtafstand) voor de halte te leggen. Na passage van dit inmeldpunt kan de chauffeur op een knop drukken om het voertuig in te melden. Als de bus halteert, vervalt deze functie en zal de bus zich automatisch inmelden zodra de chauffeur de deuren sluit, Altijd automatisch:  In- of uitmelding die automatisch wordt geactiveerd en die niet overruled kan worden door Start Deur Sluiten of halteknop."><fmt:message key="a.triggerType"/></label></td>
            <td title="Selecteer hier op welke wijze de in- of uitmelding geactiveerd kan worden.Automatisch: De in- of uitmelding wordt automatisch geactiveerd bij passage van het triggerpunt, Start Deur Sluiten: Indien zich vlak voor het kruispunt/de wegafsluiting een halte bevindt, kan het deur sluiten moment van de bus  worden gebruikt om het KAR bericht te versturen. Advies is het inmeldpunt ongeveer 75 meter (zichtafstand) voor de halte te leggen. Na passage van dit inmeldpunt kan de chauffeur op een knop drukken om het voertuig in te melden. Als de bus halteert, vervalt deze functie en zal de bus zich automatisch inmelden zodra de chauffeur de deuren sluit, Altijd automatisch:  In- of uitmelding die automatisch wordt geactiveerd en die niet overruled kan worden door Start Deur Sluiten of halteknop."><html:select property="triggerType">
                    <html:option value="PRQA"><fmt:message key="a.triggerType.PRQA"/></html:option>
                    <html:option value="PRQM"><fmt:message key="a.triggerType.PRQM"/></html:option>
                    <html:option value="SDCAS"><fmt:message key="a.triggerType.SDCAS"/></html:option>
                    <html:option value="PRQAA"><fmt:message key="a.triggerType.PRQAA"/></html:option>
                    <html:option value="PRQI"><fmt:message key="a.triggerType.PRQI"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Betreft het triggerpunt een inmeldpunt, een uitmeldpunt of een voorinmeldpunt?"><fmt:message key="a.commandType"/><br>
                <fmt:message key="a.commandType_regel2"/></label>
                </td>
            <td><html:select property="commandType" title="Betreft het triggerpunt een inmeldpunt, een uitmeldpunt of een voorinmeldpunt?">
                    <html:option value=""/>
                    <html:option value="1"><fmt:message key="a.commandType.In"/></html:option>
                    <html:option value="2"><fmt:message key="a.commandType.Uit"/></html:option>
                    <html:option value="3"><fmt:message key="a.commandType.Voor"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><span id="distanceLabel"><label title="Afstand tussen het triggerpunt en de stopstreep van de bijbehorende signaalgroep. De afstand tot stopstreep wordt automatisch ingevuld indien de locatie van het triggerpunt op de kaart is vastgelegd."><fmt:message key="a.karDistanceTillStopLine"/></label></span></td>
            <td><html:text style="border: none; width: 98%" property="karDistanceTillStopLine" title="Afstand tussen het triggerpunt en de stopstreep van de bijbehorende signaalgroep. De afstand tot stopstreep wordt automatisch ingevuld indien de locatie van het triggerpunt op de kaart is vastgelegd."/></td>
        </tr>
        <tr>
            <td><label title="Tijd tussen het triggerpunt en de stopstreep van de bijbehorende signaalgroep."><fmt:message key="a.karTimeTillStopLine"/></label></td>
            <td><html:text style="border: none; width: 98%" property="karTimeTillStopLine" title="Tijd tussen het triggerpunt en de stopstreep van de bijbehorende signaalgroep."/></td>
        </tr>
        <tr>
            <td><label title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum."><fmt:message key="updater"/></label></td>
            <td class="disabled" title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum.">
                <c:if test="${!empty activation.updater}">
                    <c:out value="${activation.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activation.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><label title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum."><fmt:message key="validator"/></label></td>
            <td class="disabled" title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum.">
                <c:if test="${!empty activation.validator}">
                    <html:img page="/images/checkmark.gif" module="" style="display: block; float: left;"/>
                    <c:out value="${activation.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activation.validationTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>

</div>
</html:form>

</c:if>