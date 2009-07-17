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
    parent.flamingo_updateKarLayer();
    parent.flamingo_cancelEdit();
</script>

<c:if test="${!hideForm}">
    
<html:form disabled="${notEditable}" styleId="activationGroupForm" action="/activationGroup" onsubmit="return validateActivationGroupForm(this)">
    <%-- geen struts tag omdat form tag disabled="true" kan zijn... maar validate submit mogelijk wel mogelijk --%>
    <input type="hidden" name="id" value="${form.id}">
    <input type="hidden" name="validated">
    <html:hidden property="copyFrom"/>
    <html:hidden property="rseqId"/>
    <html:hidden property="location"/>
    <html:submit property="save">Opslaan</html:submit>
    <c:if test="${activationCount == 1}">
        <c:set var="extraMsg">Let op! Het triggerpunt van deze signaalgroep wordt ook verwijderd!</c:set>
    </c:if>
    <c:if test="${activationCount > 1}">
        <c:set var="extraMsg">Let op! De ${activationCount} triggerpunten van deze signaalgroep worden ook verwijderd!</c:set>
    </c:if>
    <c:if test="${!empty form.id}">
        <html:submit property="delete" onclick="bCancel = true; return confirm('Weet u zeker dat u deze signaalgroep wilt verwijderen? ${extraMsg}')">Verwijderen</html:submit>
        <%-- geen struts tag omdat form tag disabled="true" kan zijn... --%>
        <input type="submit" name="validate" ${notValidatable ? 'disabled="disabled"' : ''} onclick="bCancel = true; document.forms[0].validated.value = confirm('Wilt u dit object valideren voor opname in de TMI export?');" value="Valideren">
        <html:submit property="copy">Kopi&euml;ren</html:submit>
    </c:if>
<c:set var="point" value="${activationGroup.stopLineLocationString}" scope="request"/>
<c:set var="geometryType" value="Point" scope="request"/>
<c:set var="layer" value="draw_signaalgroepen" scope="request"/>
<tiles:insert page="/WEB-INF/jsp/viewer/formEditJs.jsp"/>

<c:set var="focus" value="karSignalGroup" scope="request"/>
<tiles:insert definition="setFocus"/>

<div>
    <b>Eigenschappen</b>
    <div class="formTableContainer">
    <table class="form">
        <tr>
            <td style="width: 130px">Databeheerder</td>
            <td class="disabled"><c:out value="${rseq.dataOwner.name}"/></td>
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
            <td><fmt:message key="ag.metersAfterStopLine"/>
            <span style="font-size: 8pt"><br>meters na stopstreep</span>
            </td>
            <td>
                <html:text property="metersAfterStopLine" style="border: none; width: 98%"/>
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
            <td><fmt:message key="rseq.inactiveFrom"/></td>
            <td class="disabled">
                <c:if test="${!empty activationGroup.inactiveFrom}">
                    <fmt:formatDate pattern="dd-MM-yyyy" value="${activationGroup.inactiveFrom}"/>
                </c:if>
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