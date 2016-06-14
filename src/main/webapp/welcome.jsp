<%@include file="/WEB-INF/jsp/taglibs.jsp" %>
<%@page errorPage="/WEB-INF/jsp/commons/errorpage.jsp" %>

<div class="welcome">
    
    <h1>Welkom bij het Geo-OV platform</h1>
    <h1>Werkwijze</h1>
    <p>
    U kunt verkeerssystemen zoeken via het KAR-adres door het adres in de 'Zoeken'
    balk links in te vullen en op 'Zoek' te drukken. U kunt ook verkeerssystemen
    in de kaart aanklikken om deze te bekijken of te bewerken.
    </p>
    <p>
    Een nieuw verkeerssysteem maakt u door op een kruispunt in de kaart rechts 
    te klikken. Telkens wanneer u daarna rechts op de kaart klikt opent zich een 
    context-menu met afhankelijk van wat u hebt aangeklikt relevante menu opties.
    </p>
    <p>
    Voor het matchen van een richting van een signaalgroep aan de dienstregeling
    door vervoerders is in het koppelvlak bepaald dat bewegingen moeten worden 
    vastgelegd. Naast een uitmeldpunt moet dus verplicht &eacute;&eacute;n of meerdere 
    eindpunten worden vastgelegd waar een bus
    na het doorkruisen van het kruispunt zich heen beweegt.
    </p>
    <p>
    Nadat u een verkeerssysteem hebt geplaatst op de kaart, voegt u per 
    signaalgroep een uitmeldpunt toe. Klik rechts op het uitmeldpunt om eindpunten,
    (voor)inmeldpunten en een optioneel beginpunt toe te voegen. Door het toevoegen
    van een eindpunt wordt een nieuwe beweging gedefinieerd. De structuur van
    bewegingen wordt links weergegeven onder 'Overzicht verkeerssysteem'. In dit
    overzicht kunt u ook met rechts klikken context-menu's openen voor alle onderdelen
    van het verkeerssysteem.
    </p>
    <p>
        Een uitgebreide gebruikershandleiding is <a href="${contextPath}/Gebruikershandleiding.pdf">hier</a> te downloaden.
    </p>
    <h1>Instellingen</h1>
    <p>
    U kunt instellen welke KAR-attributen standaard moeten worden verstuurd 
    wanneer u een nieuw verkeerssysteem maakt door op de volgende knop te drukken:
    </p>
    <p>
    <input type="button" onclick="showDefaultAttributes()" value="Instellingen">
    </p>
    <h1>OV-informatie koppelvlak 1</h1>
    <p>
    Buslijnen en bushaltes zijn beschikbaar voor enkele gebieden van de volgende vervoerders. Let op!
    In de kaart kan een buslijn door ontbrekende punten in het koppelvlak van de weg afwijken.
    </p>
    <br/>
    <table border="1" style="border-collapse: collapse; border-spacing: 2px; font-size: 10pt">
        <thead>
            <tr><th><b>Vervoerder</b></th><th><b>Koppelvlak 1 import bestandsnaam</b></th></tr>
        </thead>
        <tbody id="welcomeOvInfo">
        </tbody>
    </table>
    <h1>Resolutie</h1>
    <p>
    Deze applicatie werkt het beste met een minimale resolutie van 1024x768.
    </p>
    <h1>Over deze applicatie</h1>
    <p>
        Deze applicatie is ontwikkeld door <a href="http://www.b3partners.nl/" target="_blank">B3Partners</a>.
    </p>
    <p>
        Versie: 2.6.1(11-02-2015)
    </p>
</div>
