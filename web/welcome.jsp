<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div class="welcome">
    
    <h1>Welkom bij het Geo-OV platform</h1>
      
    <p>
    Hier kan wat uitleg voor nieuwe gebruikers komen te staan.
    </p>
    
    <h1>Instellingen</h1>
    <p>
    U kunt deze applicatie aanpassen aan uw eigen wensen. Open daarvoor het
    scherm met instellingen met de volgende knop:
    </p>

    <p>
    <input type="button" onclick="settingsForm.show()" value="Instellingen">
    </p>
    
    <h1>Over deze applicatie</h1>
    <p>
        Deze applicatie is ontwikkeld door <a href="http://www.b3partners.nl/" target="_blank">B3Partners</a>.
    </p>
    <p>
        Versie: 2.0
    </p>
</div>
