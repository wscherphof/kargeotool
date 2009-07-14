<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>
<%@page import="nl.b3p.kar.hibernate.Gebruiker" %>

<h2>Gebruikersbeheer</h2>

<c:set var="form" value="${gebruikersForm.map}"/>

<html:javascript formName="gebruikersForm" staticJavascript="false"/>
<script type="text/javascript" src="<html:rewrite module="" page="/js/table.js"/>"></script>

<html:form action="/gebruikers.do" method="POST" onsubmit="return validateGebruikersForm(this)" style="\" autocomplete='off'">

<c:set var="focus" value="username" scope="request"/>
<tiles:insert definition="setFocus"/>

    <html:hidden property="id"/>
    <c:if test="${form.id != -1}">
        <html:hidden property="role"/>
        <html:hidden property="loc"/>
    </c:if>

<c:if test="${!empty gebruikers}">
    Aantal gebruikers: <b>${fn:length(gebruikers)}</b>
    <p>
    <div style="max-height: 180px; overflow: auto; padding: 1px; width: 600px">
    <table border="1" cellpadding="3" style="border-collapse: collapse" class="table-autosort:0 table-stripeclass:alternate">
        <thead>
            <tr>
                <th class="table-sortable:default">Gebruikersnaam</th>
                <th class="table-sortable:default">Naam</th>
                <th class="table-sortable:default">E-mail</th>
                <th class="table-sortable:numeric">Telefoonnummer</th>                
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="g" varStatus="status" items="${gebruikers}">
                <c:set var="editLink"><html:rewrite page="/gebruikers.do?edit=t&amp;id=${g.id}"/></c:set>
                <c:set var="col" value=""/>
                <c:if test="${g.id == form.id}">
                    <c:set var="col" value="#cccccc"/>
                </c:if>
                <tr bgcolor="${col}">
                    <td style="width: 150px"><html:link href="${editLink}"><c:out value="${g.username}"/></html:link></td>
                    <td style="width: 200px"><c:out value="${g.fullname}"/></td>
                    <td style="width: 200px"><c:out value="${g.email}"/></td>
                    <td style="width: 100px"><c:out value="${g.phone}"/></td>
                    <td>
                        <%-- niet gebruiker zichzelf laten verwijderen --%>
                        <c:if test="${g.username != pageContext.request.userPrincipal.username}">
                            <html:link page="/gebruikers.do?delete=t&amp;id=${g.id}">
                                <html:img page="/images/delete.gif" altKey="button.remove" module="" border="0"/>
                            </html:link>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>
<p>
<tiles:insert definition="infoblock"/>

    <c:if test="${!empty form.id}">
        <p>
        <c:choose>
            <c:when test="${!empty form.confirmAction}">
                <html:hidden property="confirmAction"/>
                <html:submit property="${form.confirmAction}"><fmt:message key="button.ok"/></html:submit>
                <html:cancel><fmt:message key="button.cancel"/></html:cancel>
            </c:when>
            <c:otherwise>
                <html:submit property="save"><fmt:message key="button.save"/></html:submit>
                <c:if test="${form.id != -1}">
                    <%-- niet gebruiker zichzelf laten verwijderen --%>
                    <c:if test="${form.id != pageContext.request.userPrincipal.id}">
                        <html:submit property="delete" onclick="bCancel=true;"><fmt:message key="button.remove"/></html:submit>
                    </c:if>
                </c:if>
                <html:cancel><fmt:message key="button.cancel"/></html:cancel>
            </c:otherwise>
        </c:choose>
        <p>
        <table>
            <tr>
                <td valign="top">
        <table>
            <tr>
                <td><fmt:message key="gebruiker.username"/></td>
                <td><html:text property="username" size="20" maxlength="30"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.fullName"/></td>
                <td><html:text property="fullName" size="30" maxlength="50"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.email"/></td>
                <td><html:text property="email" size="30" maxlength="50"/></td>
            </tr>
            <tr>
                <td><fmt:message key="gebruiker.password"/></td>
                <td>
                    <html:password property="password" size="20" maxlength="50"/>
                </td>
            </tr>
            <c:if test="${form.id != -1}">
                <tr>
                    <td></td>
                    <td><i>Laat dit veld leeg om het wachtwoord niet te wijzigen.</i></td>
                </tr>
            </c:if>
            <tr>
                <td><fmt:message key="gebruiker.phone"/></td>
                <td><html:text property="phone" size="15" maxlength="15"/></td>
            </tr>            
            <tr>
                <td style="vertical-align: top"><fmt:message key="gebruiker.role"/></td>
                <td>
                    <c:forEach var="r" items="${availableRoles}">
                        <html:multibox property="roles" value="${r.id}"/><c:out value="${r.role}"/><br>
                    </c:forEach>
                </td>

            </tr>
        </table>
                </td>
                <td valign="top">
                    <c:set var="isBeheerder" value="${false}"/>
                    <c:if test="${!empty gebruiker}">
                        <c:set var="isBeheerder"><%= ((Gebruiker)request.getAttribute("gebruiker")).isInRole("beheerder") %></c:set>
                    </c:if>
            <div id="roListHeader">
                <div id="beheerder" style="display: ${isBeheerder ? 'block' : 'none'} ">
                    Een beheerder kan van alle wegbeheerders gegevens bewerken en valideren.
                </div>
                <div id="nietBeheerder" style="display: ${isBeheerder ? 'none' : 'block'}">
                    Gebruiker kan gegevens van de onderstaande wegbeheerders bewerken of valideren:
                </div>
            </div>
            <div id="roList">
                <table id="roListTable">
                    <tr>
                        <th style="width: 35px">Code</th>
                        <th style="width: 160px">Naam</th>
                        <th style="width: 60px">Bewerken</th>
                        <th style="width: 60px">Valideren</th>
                    </tr>
                    <c:forEach var="dor" items="${dataOwnerRights}">
                        <tr>
                            <td>${dor.dataOwner.code}</td>
                            <td>${dor.dataOwner.name}</td>
                            <td style="text-align: center"><html:multibox property="dataOwnersEditable" value="${dor.dataOwner.id}" onclick="this.blur()" onchange="checkDORemove(event)"/></td>
                            <td style="text-align: center"><html:multibox property="dataOwnersValidatable" value="${dor.dataOwner.id}" onclick="this.blur()" onchange="checkDORemove(event)"/></td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
                    Toevoegen wegbeheerder:<br>
                    <select id="availableDataOwners" onchange="checkDOAdd()">
                        <option>Selecteer een wegbeheerder...
                    </select>
<script type="text/javascript">
    setOnload(initAvailableDataOwners);

    var dataOwners = ${dataOwnersJson};
    var usedDataOwners = {};

    var codeNameSeparator = " - ";

    function initAvailableDataOwners() {
        usedDataOwners = {};
        var editable = document.forms[0].dataOwnersEditable;
        var validatable = document.forms[0].dataOwnersValidatable;
        for(var i = 0; i < editable.length; i++) {
            var value = editable[i].value;
            if(editable[i].checked || validatable[i].checked) {
                usedDataOwners[value] = true;                
            }
        }

        var availableDO = document.forms[0].availableDataOwners;
        for(var i = 0; i < dataOwners.length; i++) {
            var dao = dataOwners[i];
            var used = usedDataOwners[dao.id + ""] ? true : false;
            if(!used) {
                availableDO.options[availableDO.length] = new Option(dao.code + codeNameSeparator + dao.name, dao.id);
            }
        }
    }

    function checkDOAdd() {
        var availableDO = document.forms[0].availableDataOwners;
        var selectedIndex = availableDO.selectedIndex;
        if(selectedIndex > 0) {
            var value = parseInt(availableDO.options[selectedIndex].value);
            var text = availableDO.options[selectedIndex].text;
            var code = text.substring(0, text.indexOf(codeNameSeparator)); /* XXX deze separator is hardcoded... */
            var name = text.substring(text.indexOf(codeNameSeparator) + codeNameSeparator.length, text.length);

            addDataOwner(value, code, name);

            availableDO.remove(selectedIndex);
            
            availableDO.selectedIndex = 0;
        }
    }

    function checkDORemove(e) {
        if(!e) var e = window.event;
        var target = e.target ? e.target : e.srcElement;

        var id = target.value;

        var bothUnchecked = !isChecked("dataOwnersEditable", id) && !isChecked("dataOwnersValidatable", id);

        if(bothUnchecked) {
            if(confirm("Wilt u deze wegbeheerder uit de lijst verwijderen?")) {
                removeDataOwner(id);
            } else {
                target.checked = true;
            }
        }
    }

    function isChecked(name, value) {
        var options = document.forms[0][name];
        for(var i = 0; i < options.length; i++) {
            if(options[i].value == value) {
                return options[i].checked;
            }
        }
        return undefined;
    }
    
    function addDataOwner(id, code, name) {
        /* zoek positie waarop table row geinsert moet worden */
        var table = document.getElementById("roListTable");
        var index = table.rows.length;
        for(var i = 0; i < table.rows.length; i++) {
            var rowValue = table.rows[i].cells[2].firstChild.value;
            if(id < rowValue) {
                index = i;
                break;
            }
        }

        var row = table.insertRow(index);
        /* helaas is row.innerHTML in IE read-only... */
        var cell = row.insertCell(0);
        cell.appendChild(document.createTextNode(code));
        cell = row.insertCell(1);
        cell.appendChild(document.createTextNode(name));
        cell = row.insertCell(2);
        cell.style.textAlign = "center";
        var input = document.createElement("input");
        input.name = "dataOwnersEditable";
        input.type = "checkbox";
        input.value = id + "";
        input.checked = true;
        input.onchange = checkDORemove;
        input.onclick = function() { this.blur() };
        cell.appendChild(input);
        cell = row.insertCell(3);
        cell.style.textAlign = "center";
        input = document.createElement("input");
        input.name = "dataOwnersValidatable";
        input.type = "checkbox";
        input.value = id + "";
        input.checked = false;
        input.onchange = checkDORemove;
        input.onclick = function() { this.blur() };
        cell.appendChild(input);
    }

    function removeDataOwner(id) {
        var code, name;

        var table = document.getElementById("roListTable");
        for(var i = 0; i < table.rows.length; i++) {
            var rowValue = table.rows[i].cells[2].firstChild.value;
            if(id == rowValue) {
                code = table.rows[i].cells[0].firstChild.nodeValue;
                name = table.rows[i].cells[1].firstChild.nodeValue;
                table.deleteRow(i);
                break;
            }
        }
        
        /* zoek positie waarop select option geinsert moet worden */
        var availableDO = document.forms[0].availableDataOwners;
        var insertBefore = null;
        for(var i = 0; i < availableDO.options.length; i++) {
            if(parseInt(id) < parseInt(availableDO.options[i].value)) {
                insertBefore = availableDO.options[i];
                break;
            }
         }
        var option = new Option(code + codeNameSeparator + name, id);
        availableDO.add(option, insertBefore);
    }

</script>


                </td>
            </tr>
        </table>


    </c:if>
    <c:if test="${empty form.id}">
        <html:submit property="create" onclick="bCancel=true;">Nieuw account toevoegen</html:submit>
    </c:if>

</c:if>

</html:form>