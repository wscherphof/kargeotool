
-- Afkomstig uit "TMI8 KAR meldpunten (kv 9), v8.1.0.0, release.pdf", pagina 16

insert into data_owner(code, classificatie, company_number, omschrijving) values
 ('ARR', 'Vervoerder', 1, 'Arriva Personenvervoer Nederland'),
 ('VTN', 'Vervoerder', 2, 'Veolia Transport Nederland'),
 ('CXX', 'Vervoerder', 3, 'Connexxion, Hermes (incl GVU, Novio, Stadsvervoer NL)'),
 ('GVB', 'Vervoerder', 4, 'Gemeente Vervoerbedrijf Amsterdam'),
 ('HTM', 'Vervoerder', 5, 'Haagse Tramweg Maatschappij'),
 ('RET', 'Vervoerder', 6, 'RET'),
 ('NS', 'Vervoerder', 7, 'Nederlandse Spoorwegen'),
 ('SYNTUS', 'Vervoerder', 8, 'Syntus'),
 ('QBUZZ', 'Vervoerder', 9, 'Qbuzz'),
 ('TCR', 'Vervoerder', 10, 'Taxi Centrale Renesse'),
 ('ALGEMEEN', 'Integrator (deprecated)', null, 'Algemene integrator (deprecated)'),
 ('DRECHTSTED', 'Integrator', null, 'Drechtsteden'),
 ('GOVI', 'Integrator', null, 'Grenzeloze OV Informatie'),
 ('RIG', 'Integrator', null, 'Reis Informatie Groep'),
 ('SABIMOS', 'Integrator', null, 'Satellite Based Information and Management Operating System'),
 ('PRORAIL', 'Integrator', null, 'ProRail');
 
insert into data_owner(code, classificatie, company_number, omschrijving) values
 ('B3P', 'Integrator', null, 'B3Partners');

insert into data_owner(code, classificatie, company_number, omschrijving) select
    'CBSPV' || unnest(ARRAY['0001','0002','0003','0004','0005','0006','0007','0008','0009','0010','0011','0012']),
    'Infra Beheerder',
    null,
    unnest(ARRAY['Provincie Groningen','Provincie Fryslân','Provincie Drenthe','Provincie Overijssel','Provincie Gelderland','Provincie Utrecht','Provincie Noord-Holland','Provincie Zuid-Holland','Provincie Zeeland','Provincie Noord-Brabant','Provincie Limburg','Provincie Flevoland']);

insert into data_owner(code, classificatie, company_number, omschrijving) select
    'RWS' || unnest(ARRAY['BD','DVS','DID','WD','DNH','DZH','DUT','DIJG','DNN','DON','DZL','DNB','DLB','DNZ']),
    'Infra Beheerder',
    null,
    unnest(ARRAY['Landelijke Bouw Dienst','Landelijk Dienst Verkeer en Scheepvaart','Landelijke Data en Informatie Dienst',
    'Landelijke Water Dienst','Regionale Dienst Noord-Holland','Regionale Dienst Zuid-Holland','Regionale Dienst Utrecht',
    'Regionale Dienst IJsselmeer Gebied','Regionale Dienst Noord-Nederland','Regionale Dienst Oost-Nederland',
    'Regionale Dienst Zeeland','Regionale Dienst Noord-Brabant','Regionale Dienst Limburg','Regionale Dienst Noordzee']);
    
insert into data_owner(code, classificatie, company_number, omschrijving) select
    'INFRA' || unnest(ARRAY['AAS','HBR']),
    'Infra Beheerder',
    null,
    unnest(ARRAY['Amsterdam Airport Schiphol','Havenbedrijf Rotterdam']);

    












    
    

