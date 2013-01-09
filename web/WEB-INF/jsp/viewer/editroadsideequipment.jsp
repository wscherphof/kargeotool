<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<c:set var="form" value="${roadsideEquipmentForm.map}"/>

<tiles:insert definition="infoblock"/>

<html:javascript formName="roadsideEquipmentForm" staticJavascript="false"/>

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

<html:form disabled="${notEditable}" styleId="roadsideEquipmentForm" action="/roadsideEquipment" onsubmit="return validateRoadsideEquipmentForm(this)">
    <%-- geen struts tag omdat form tag disabled="true" kan zijn... maar validate submit mogelijk wel mogelijk --%>
    <input type="hidden" name="id" value="${form.id}">
    <input type="hidden" name="validated">
    <input type="hidden" name="validatedAll">
    <html:hidden property="location"/>
    <html:submit property="save" title="Sla de wijzigingen steeds op per walapparaat, signaalgroep of triggerpunt.">Opslaan</html:submit>
    <c:if test="${activationGroupCount == 1}">
        <c:set var="extraMsg">Let op! De signaalgroep (en alle triggerpunten hiervan) van deze walapparatuur wordt ook verwijderd!</c:set>
    </c:if>
    <c:if test="${activationGroupCount > 1}">
        <c:set var="extraMsg">Let op! De ${activationGroupCount} signaalgroepen (en alle triggerpunten daarvan) van deze walapparatuur worden ook verwijderd!</c:set>
    </c:if>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" title="Het walapparaat inclusief bijbehorende signaalgroepen en triggerpunten verwijderen." onclick="bCancel = true; return confirm('Weet u zeker dat u deze walapparatuur wilt verwijderen? ${extraMsg}')">Verwijderen</html:submit>
        <%-- geen struts tag omdat form tag disabled="true" kan zijn... --%>
        <input type="submit" title="De data van het walapparaat valideren." name="validate" ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');" value="Valideren">
        <input type="submit" title="De data van het walapparaat inclusief de data van de bijbehorende signaalgroepen en triggerpunten valideren." name="validateAll" ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validatedAll.value = confirm('Wilt u alle objecten valideren voor opname in de TMI export?');" value="Allemaal valideren">
    </c:if>
    
<c:set var="point" value="${roadsideEquipment.locationString}" scope="request"/>
<c:set var="geometryType" value="Point" scope="request"/>
<tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>

<c:set var="focus" value="unitNumber" scope="request"/>
<tiles:insert definition="setFocus"/>

<div>
    <b>Eigenschappen walapparatuur</b>
    <div class="formTableContainer">
        <table class="form">
            <tr>
                <td style="width: 130px"><label title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data."><fmt:message key="rseq.dataOwner"/></label></td>
            <td>
                <html:select property="dataOwner" title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data." style="width: 100%">
                    <c:if test="${empty form.id}">
                        <html:option value=""/>
                    </c:if>
                    <c:forEach items="${dataOwners}" var="dataOwner">
                        <html:option value="${dataOwner.id}"><c:out value="${dataOwner.name}"/></html:option>
                    </c:forEach>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Selecteer hier voor welk type walapparaat u data wilt invoeren: CROSS: VRI van een kruispunt, CLOSE: selectieve afsluiting (d.m.v. een slagboom of inzinkbare paal, PIU: Passenger Information Unit (halteprocessor)">
<fmt:message key="rseq.type"/></label></td>
            <td>
                <html:select title="Selecteer hier voor welk type walapparaat u data wilt invoeren: CROSS: VRI van een kruispunt, CLOSE: selectieve afsluiting (d.m.v. een slagboom of inzinkbare paal, PIU: Passenger Information Unit (halteprocessor)" property="type">
                    <c:if test="${empty form.id}">
                        <html:option value=""/>
                    </c:if>
                    <html:option value="CROSS">CROSS - VRI</html:option>
                    <html:option value="CLOSE">CLOSE - selectieve afsluiting</html:option>
                    <html:option value="PIU">PIU - halteprocessor</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)." ><fmt:message key="rseq.unitNumber"/></label></td>
            <td>
                <html:text property="unitNumber" title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)." style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td><label title="Adres waarmee het walapparaat de voor hem bestemde KAR-berichten kan herkennen. Wordt ook SID (System IDentification) genoemd."> <fmt:message key="rseq.radioAddress"/></label></td>
            <td>
                <html:text title="Adres waarmee het walapparaat de voor hem bestemde KAR-berichten kan herkennen. Wordt ook SID (System IDentification) genoemd." property="radioAddress" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="Omschrijving van de locatie van het walapparaat. Notatiewijze: Plaatsnaam, weg 1 - weg 2"><fmt:message key="rseq.description"/></label></td>
            <td class="number">
                <html:textarea rows="2" title="Omschrijving van de locatie van het walapparaat. Notatiewijze: Plaatsnaam, weg 1 - weg 2" style="width: 100%; border: none; font-family: tahoma, sans-serif; font-size: 8pt" property="description"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="De leverancier van het walapparaat."><fmt:message key="rseq.supplier"/></label></td>
            <td>
                 <html:text title="De leverancier van het walapparaat." property="supplier" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="Volgens nummering leverancier."><fmt:message key="rseq.supplierTypeNumber"/></label></td>
            <td>
                <html:text title="Volgens nummering leverancier." property="supplierTypeNumber" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top"><label title="Notatiewijze: dd-mm-jjjj"><fmt:message key="rseq.installationDate"/></label></td>
            <td>
                <html:text property="installationDate" title="Notatiewijze: dd-mm-jjjj" style="border: none; width: 98%"/>
            </td>
        </tr>
        <tr>
            <td><label title="Is er VETAG-, VECOM- of SICS-detectie naast KAR aanwezig?"><fmt:message key="rseq.selectiveDetectionLoop"/></label></td>
            <td>
                <html:select title="Is er VETAG-, VECOM- of SICS-detectie naast KAR aanwezig?" property="selectiveDetectionLoop">
                    <html:option value="false">Nee</html:option>
                    <html:option value="true">Ja</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td><label title="Datum waarop het walapparaat volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj"><fmt:message key="rseq.inactiveFrom"/></label></td>
            <td class="disabled" title="Datum waarop het walapparaat volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj">
                <c:if test="${!empty roadsideEquipment.inactiveFrom}">
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.inactiveFrom}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><label title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum."> <fmt:message key="updater"/></label></td>
            <td class="disabled" title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum.">
                <c:if test="${!empty roadsideEquipment.updater}">
                    <c:out value="${roadsideEquipment.updater}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.updateTime}"/>
                </c:if>
            </td>
        </tr>
        <tr>
            <td><label title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum."><fmt:message key="validator"/></label></td>
            <td class="disabled" title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum.">
                <c:if test="${!empty roadsideEquipment.validator}">
                    <html:img page="/images/checkmark.gif" module="" style="display: block; float: left;"/>
                    <c:out value="${roadsideEquipment.validator}"/> op
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${roadsideEquipment.validationTime}"/>
                </c:if>
            </td>
        </tr>
    </table>
    </div>
</div>

</html:form>

</c:if>