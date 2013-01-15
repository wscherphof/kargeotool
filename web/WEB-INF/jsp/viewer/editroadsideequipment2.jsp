<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<!DOCTYPE html> 
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/commons/siteTemplate2.jsp">

    <stripes:layout-component name="content">        
        <c:if test="${!empty actionBean.treeUpdate}">
            <script type="text/javascript">
                parent.treeUpdate('${fn:replace(treeUpdate,"'","\\'")}');
            </script>
        </c:if>
        <script type="text/javascript">
            parent.cancelEdit();

            var agIds = "${actionBean.rseq.activationGroupIds}";
            var aIds = "${actionBean.activationIds}";
            var rseqId = "${actionBean.rseq.id}";
    
            parent.showSelected( rseqId, agIds, aIds);

            parent.oc.update();

            <c:if test="${actionBean.notEditable}">
                parent.notEditable();
            </c:if>
        </script>
        <c:if test="${!actionBean.hideForm}">
            <%--/*disabled="${actionBean.notEditable}"*/ --%>
            <stripes:form action="/action/edit/roadsideequiment" onsubmit="return validateRoadsideEquipmentForm(this)">
                <%-- geen struts tag omdat form tag disabled="true" kan zijn... maar validate submit mogelijk wel mogelijk --%>
                <stripes:hidden name="id" value="${actionBean.rseq.id}"/>
                <stripes:hidden name="validated"/>
                <stripes:hidden name="validatedAll"/>
                <stripes:hidden name="location"/>
                <stripes:submit name="save" title="Sla de wijzigingen steeds op per walapparaat, signaalgroep of triggerpunt.">Opslaan</stripes:submit>
                <c:if test="${actionBean.activationGroupCount == 1}">
                    <c:set var="extraMsg">Let op! De signaalgroep (en alle triggerpunten hiervan) van deze walapparatuur wordt ook verwijderd!</c:set>
                </c:if>
                <c:if test="${actionBean.activationGroupCount > 1}">
                    <c:set var="extraMsg">Let op! De ${actionBean.activationGroupCount} signaalgroepen (en alle triggerpunten daarvan) van deze walapparatuur worden ook verwijderd!</c:set>
                </c:if>
                <c:if test="${!empty actionBean.rseq.id}">
                    <stripes:submit name="delete" title="Het walapparaat inclusief bijbehorende signaalgroepen en triggerpunten verwijderen." onclick="bCancel = true; return confirm('Weet u zeker dat u deze walapparatuur wilt verwijderen? ${extraMsg}')">Verwijderen</stripes:submit>
                    <%-- geen struts tag omdat form tag disabled="true" kan zijn... --%>
                    <input type="submit" title="De data van het walapparaat valideren." name="validate" ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');" value="Valideren">
                    <input type="submit" title="De data van het walapparaat inclusief de data van de bijbehorende signaalgroepen en triggerpunten valideren." name="validateAll" ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validatedAll.value = confirm('Wilt u alle objecten valideren voor opname in de TMI export?');" value="Allemaal valideren">
                </c:if>

                <c:set var="point" value="${actionBean.rseq.locationString}" scope="request"/>
                <c:set var="geometryType" value="Point" scope="request"/>
                <%--tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/--%>
                <stripes:layout-component name="formEdit" >
                    <%@include file="/WEB-INF/jsp/viewer/formEditJs2.jsp" %>
                </stripes:layout-component>
                <%--c:set var="focus" value="unitNumber" scope="request"/>
                <tiles:insert definition="setFocus"/--%>

                <div>
                    <b>Eigenschappen walapparatuur</b>
                    <div class="formTableContainer">
                        <table class="form">
                            <tr>
                                <td style="width: 130px"><label title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data."><fmt:message key="rseq.dataOwner"/></label></td>
                                <td>
                                    <stripes:select name="dataOwner" title="De wegbeheerder (gemeente, provincie, waterschap, etc) die eigenaar is van /verantwoordelijk is voor de data." style="width: 100%">
                                        <stripes:option value=""/>
                                        <c:forEach items="${actionBean.dataOwners}" var="dataOwner">
                                            <stripes:option value="${actionBean.dataOwner.id}"><c:out value="${actionBean.dataOwner.name}"/></stripes:option>
                                        </c:forEach>
                                    </stripes:select>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Selecteer hier voor welk type walapparaat u data wilt invoeren: CROSS: VRI van een kruispunt, CLOSE: selectieve afsluiting (d.m.v. een slagboom of inzinkbare paal, PIU: Passenger Information Unit (halteprocessor)">
                                        <fmt:message key="rseq.type"/></label></td>
                                <td>
                                    <stripes:select title="Selecteer hier voor welk type walapparaat u data wilt invoeren: CROSS: VRI van een kruispunt, CLOSE: selectieve afsluiting (d.m.v. een slagboom of inzinkbare paal, PIU: Passenger Information Unit (halteprocessor)" name="type">
                                        <c:if test="${empty actionBean.rseq.id}">
                                            <stripes:option value=""/>
                                        </c:if>
                                        <stripes:option value="CROSS">CROSS - VRI</stripes:option>
                                        <stripes:option value="CLOSE">CLOSE - selectieve afsluiting</stripes:option>
                                        <stripes:option value="PIU">PIU - halteprocessor</stripes:option>
                                    </stripes:select>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)." ><fmt:message key="rseq.unitNumber"/></label></td>
                                <td>
                                    <stripes:text name="unitNumber" title="Nummer dat de wegbeheerder toewijst aan het walapparaat (bij VRI's is dit nummer het kruispuntnummer)." style="border: none; width: 98%"/>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Adres waarmee het walapparaat de voor hem bestemde KAR-berichten kan herkennen. Wordt ook SID (System IDentification) genoemd."> <fmt:message key="rseq.radioAddress"/></label></td>
                                <td>
                                    <stripes:text title="Adres waarmee het walapparaat de voor hem bestemde KAR-berichten kan herkennen. Wordt ook SID (System IDentification) genoemd." name="radioAddress" style="border: none; width: 98%"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="vertical-align: top"><label title="Omschrijving van de locatie van het walapparaat. Notatiewijze: Plaatsnaam, weg 1 - weg 2"><fmt:message key="rseq.description"/></label></td>
                                <td class="number">
                                    <stripes:textarea rows="2" title="Omschrijving van de locatie van het walapparaat. Notatiewijze: Plaatsnaam, weg 1 - weg 2" style="width: 100%; border: none; font-family: tahoma, sans-serif; font-size: 8pt" name="description"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="vertical-align: top"><label title="De leverancier van het walapparaat."><fmt:message key="rseq.supplier"/></label></td>
                                <td>
                                    <stripes:text title="De leverancier van het walapparaat." name="supplier" style="border: none; width: 98%"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="vertical-align: top"><label title="Volgens nummering leverancier."><fmt:message key="rseq.supplierTypeNumber"/></label></td>
                                <td>
                                    <stripes:text title="Volgens nummering leverancier." name="supplierTypeNumber" style="border: none; width: 98%"/>
                                </td>
                            </tr>
                            <tr>
                                <td style="vertical-align: top"><label title="Notatiewijze: dd-mm-jjjj"><fmt:message key="rseq.installationDate"/></label></td>
                                <td>
                                    <stripes:text name="installationDate" title="Notatiewijze: dd-mm-jjjj" style="border: none; width: 98%"/>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Is er VETAG-, VECOM- of SICS-detectie naast KAR aanwezig?"><fmt:message key="rseq.selectiveDetectionLoop"/></label></td>
                                <td>
                                    <stripes:select title="Is er VETAG-, VECOM- of SICS-detectie naast KAR aanwezig?" name="selectiveDetectionLoop">
                                        <stripes:option value="false">Nee</stripes:option>
                                        <stripes:option value="true">Ja</stripes:option>
                                    </stripes:select>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Datum waarop het walapparaat volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj"><fmt:message key="rseq.inactiveFrom"/></label></td>
                                <td class="disabled" title="Datum waarop het walapparaat volgens planning buiten bedrijf is/wordt gesteld. Notatiewijze: dd-mm-jjjj">
                                    <c:if test="${!empty actionBean.rseq.inactiveFrom}">
                                        <fmt:formatDate pattern="dd-MM-yyyy" value="${actionBean.rseq.inactiveFrom}"/>
                                    </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum."> <fmt:message key="updater"/></label></td>
                                <td class="disabled" title="Persoon die de data heeft gemuteerd met de laatste mutatiedatum.">
                                    <c:if test="${!empty actionBean.rseq.updater}">
                                        <c:out value="${actionBean.rseq.updater}"/> op
                                        <fmt:formatDate pattern="dd-MM-yyyy" value="${actionBean.rseq.updateTime}"/>
                                    </c:if>
                                </td>
                            </tr>
                            <tr>
                                <td><label title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum."><fmt:message key="validator"/></label></td>
                                <td class="disabled" title="Persoon die de data van heeft gevalideerd met de laatste validatiedatum.">
                                    <c:if test="${!empty actionBean.rseq.validator}">
                                        <img page="/images/checkmark.gif" module="" style="display: block; float: left;"/>
                                        <c:out value="${actionBean.rseq.validator}"/> op
                                        <fmt:formatDate pattern="dd-MM-yyyy" value="${actionBean.rseq.validationTime}"/>
                                    </c:if>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>

            </stripes:form>

        </c:if>

    </stripes:layout-component>
</stripes:layout-render>