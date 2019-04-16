alter table data_owner add constraint  codeUnique UNIQUE(code);
-- nieuwe shape inladen in gemeentetabel
-- nieuwe dataowner toevoegen aan dataowner tabel
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1945', 'Gemeente Berg en Dal');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1930', 'Gemeente Nissewaard');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1948', 'Gemeente Meierijstad');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1954', 'Gemeente Beekdaelen');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1959', 'Gemeente Altena');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1960', 'Gemeente West Betuwe');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1961', 'Gemeente Vijfheerenlanden');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1978', 'Gemeente Molenlanden');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1963', 'Gemeente Hoeksche Waard');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1966', 'Gemeente Het Hogeland');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1969', 'Gemeente Westerkwartier');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1970', 'Gemeente Noardeast-Fryslân');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1931', 'Gemeente Krimpenerwaard');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1940', 'Gemeente De Fryske Marren');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1942', 'Gemeente Gooise Meren');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1950', 'Gemeente Westerwolde');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1952', 'Gemeente Midden-Groningen');
insert into data_owner (classificatie,code, omschrijving) values ('Infra Beheerder','CBSGM1949', 'Gemeente Waadhoeke');

-- rseq laten wijzen naar nieuwe dataownercode
update roadside_equipment set data_owner = (select id from data_owner where code ='CBSGM1945') where data_owner in (select id from data_owner where code in ('CBSGM0241','CBSGM0265','CBSGM0282'));
update roadside_equipment set data_owner = (select id from data_owner where code ='CBSGM1930') where data_owner in (select id from data_owner where code in ('CBSGM0612','CBSGM0568'));
update roadside_equipment set data_owner = (select id from data_owner where code ='CBSGM1948') where data_owner in (select id from data_owner where code in ('CBSGM0860','CBSGM0844','CBSGM0846'));

-- upload laten wijzen naar nieuwe dataownercode

update upload set data_owner = (select id from data_owner where code ='CBSGM1945') where data_owner in (select id from data_owner where code in ('CBSGM0241','CBSGM0265','CBSGM0282'));
update upload set data_owner = (select id from data_owner where code ='CBSGM1930') where data_owner in (select id from data_owner where code in ('CBSGM0612','CBSGM0568'));
update upload set data_owner = (select id from data_owner where code ='CBSGM1948') where data_owner in (select id from data_owner where code in ('CBSGM0860','CBSGM0844','CBSGM0846'));
-- gebruiker_data_owner_rights laten wijzen naar nieuwe dataownercode

update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code ='CBSGM1945') where data_owner in (select id from data_owner where code in ('CBSGM0241','CBSGM0265','CBSGM0282'));
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code ='CBSGM1930') where data_owner in (select id from data_owner where code in ('CBSGM0612','CBSGM0568'));
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code ='CBSGM1948') where data_owner in (select id from data_owner where code in ('CBSGM0860','CBSGM0844','CBSGM0846'));


-- rseq laten wijzen naar nieuwe dataownercode

update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0361')	where data_owner in (select id from data_owner where code = 'CBSGM0365');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0361')	where data_owner in (select id from data_owner where code = 'CBSGM0458');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1945')	where data_owner in (select id from data_owner where code = 'CBSGM0265');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1945')	where data_owner in (select id from data_owner where code = 'CBSGM0282');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1930')	where data_owner in (select id from data_owner where code = 'CBSGM0568');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1930')	where data_owner in (select id from data_owner where code = 'CBSGM0612');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1931')	where data_owner in (select id from data_owner where code = 'CBSGM0491');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1931')	where data_owner in (select id from data_owner where code = 'CBSGM0643');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1931')	where data_owner in (select id from data_owner where code = 'CBSGM0644');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1931')	where data_owner in (select id from data_owner where code = 'CBSGM0608');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1931')	where data_owner in (select id from data_owner where code = 'CBSGM0623');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0796')	where data_owner in (select id from data_owner where code = 'CBSGM1671');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0828')	where data_owner in (select id from data_owner where code = 'CBSGM1671');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1940')	where data_owner in (select id from data_owner where code = 'CBSGM1921');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1945')	where data_owner in (select id from data_owner where code = 'CBSGM0241');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1942')	where data_owner in (select id from data_owner where code = 'CBSGM0381');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1942')	where data_owner in (select id from data_owner where code = 'CBSGM0424');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1942')	where data_owner in (select id from data_owner where code = 'CBSGM0425');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0385')	where data_owner in (select id from data_owner where code = 'CBSGM0478');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1948')	where data_owner in (select id from data_owner where code = 'CBSGM0844');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1948')	where data_owner in (select id from data_owner where code = 'CBSGM0846');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1948')	where data_owner in (select id from data_owner where code = 'CBSGM0860');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1950')	where data_owner in (select id from data_owner where code = 'CBSGM0007');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1950')	where data_owner in (select id from data_owner where code = 'CBSGM0048');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1952')	where data_owner in (select id from data_owner where code = 'CBSGM0018');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1952')	where data_owner in (select id from data_owner where code = 'CBSGM0040');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1952')	where data_owner in (select id from data_owner where code = 'CBSGM1987');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1949')	where data_owner in (select id from data_owner where code = 'CBSGM0063');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1949')	where data_owner in (select id from data_owner where code = 'CBSGM0070');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1949')	where data_owner in (select id from data_owner where code = 'CBSGM1908');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0080')	where data_owner in (select id from data_owner where code = 'CBSGM0140');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1900')	where data_owner in (select id from data_owner where code = 'CBSGM0140');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1949')	where data_owner in (select id from data_owner where code = 'CBSGM0140');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0080')	where data_owner in (select id from data_owner where code = 'CBSGM0081');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0299')	where data_owner in (select id from data_owner where code = 'CBSGM0196');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1954')	where data_owner in (select id from data_owner where code = 'CBSGM0951');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1954')	where data_owner in (select id from data_owner where code = 'CBSGM0881');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1954')	where data_owner in (select id from data_owner where code = 'CBSGM0962');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1959')	where data_owner in (select id from data_owner where code = 'CBSGM0738');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1959')	where data_owner in (select id from data_owner where code = 'CBSGM0870');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1959')	where data_owner in (select id from data_owner where code = 'CBSGM0874');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1960')	where data_owner in (select id from data_owner where code = 'CBSGM0236');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1960')	where data_owner in (select id from data_owner where code = 'CBSGM0304');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1960')	where data_owner in (select id from data_owner where code = 'CBSGM0733');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1961')	where data_owner in (select id from data_owner where code = 'CBSGM0545');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1961')	where data_owner in (select id from data_owner where code = 'CBSGM0707');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1961')	where data_owner in (select id from data_owner where code = 'CBSGM0620');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1978')	where data_owner in (select id from data_owner where code = 'CBSGM1927');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1978')	where data_owner in (select id from data_owner where code = 'CBSGM0689');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0575')	where data_owner in (select id from data_owner where code = 'CBSGM0576');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1963')	where data_owner in (select id from data_owner where code = 'CBSGM0585');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1963')	where data_owner in (select id from data_owner where code = 'CBSGM0611');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1963')	where data_owner in (select id from data_owner where code = 'CBSGM0588');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1963')	where data_owner in (select id from data_owner where code = 'CBSGM0584');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1963')	where data_owner in (select id from data_owner where code = 'CBSGM0617');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0394')	where data_owner in (select id from data_owner where code = 'CBSGM0393');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1966')	where data_owner in (select id from data_owner where code = 'CBSGM0005');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1966')	where data_owner in (select id from data_owner where code = 'CBSGM1663');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1966')	where data_owner in (select id from data_owner where code = 'CBSGM1651');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1966')	where data_owner in (select id from data_owner where code = 'CBSGM0053');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1969')	where data_owner in (select id from data_owner where code = 'CBSGM0015');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1969')	where data_owner in (select id from data_owner where code = 'CBSGM0022');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1969')	where data_owner in (select id from data_owner where code = 'CBSGM0025');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1969')	where data_owner in (select id from data_owner where code = 'CBSGM0056');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0014')	where data_owner in (select id from data_owner where code = 'CBSGM0009');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM0014')	where data_owner in (select id from data_owner where code = 'CBSGM0017');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1970')	where data_owner in (select id from data_owner where code = 'CBSGM0058');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1970')	where data_owner in (select id from data_owner where code = 'CBSGM1722');
update roadside_equipment set data_owner = (select id from data_owner where code = 'CBSGM1970')	where data_owner in (select id from data_owner where code = 'CBSGM0079');


-- upload laten wijzen naar nieuwe dataownercode


update upload set data_owner = (select id from data_owner where code = 'CBSGM0361') where data_owner in (select id from data_owner where code = 'CBSGM0365');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0361') where data_owner in (select id from data_owner where code = 'CBSGM0458');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code = 'CBSGM0265');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code = 'CBSGM0282');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1930') where data_owner in (select id from data_owner where code = 'CBSGM0568');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1930') where data_owner in (select id from data_owner where code = 'CBSGM0612');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code = 'CBSGM0491');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code = 'CBSGM0643');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code = 'CBSGM0644');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code = 'CBSGM0608');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code = 'CBSGM0623');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0796') where data_owner in (select id from data_owner where code = 'CBSGM1671');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0828') where data_owner in (select id from data_owner where code = 'CBSGM1671');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1940') where data_owner in (select id from data_owner where code = 'CBSGM1921');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code = 'CBSGM0241');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code = 'CBSGM0381');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code = 'CBSGM0424');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code = 'CBSGM0425');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0385') where data_owner in (select id from data_owner where code = 'CBSGM0478');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code = 'CBSGM0844');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code = 'CBSGM0846');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code = 'CBSGM0860');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1950') where data_owner in (select id from data_owner where code = 'CBSGM0007');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1950') where data_owner in (select id from data_owner where code = 'CBSGM0048');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code = 'CBSGM0018');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code = 'CBSGM0040');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code = 'CBSGM1987');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code = 'CBSGM0063');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code = 'CBSGM0070');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code = 'CBSGM1908');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0080') where data_owner in (select id from data_owner where code = 'CBSGM0140');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1900') where data_owner in (select id from data_owner where code = 'CBSGM0140');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code = 'CBSGM0140');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0080') where data_owner in (select id from data_owner where code = 'CBSGM0081');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0299') where data_owner in (select id from data_owner where code = 'CBSGM0196');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code = 'CBSGM0951');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code = 'CBSGM0881');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code = 'CBSGM0962');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code = 'CBSGM0738');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code = 'CBSGM0870');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code = 'CBSGM0874');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code = 'CBSGM0236');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code = 'CBSGM0304');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code = 'CBSGM0733');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code = 'CBSGM0545');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code = 'CBSGM0707');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code = 'CBSGM0620');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1978') where data_owner in (select id from data_owner where code = 'CBSGM1927');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1978') where data_owner in (select id from data_owner where code = 'CBSGM0689');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0575') where data_owner in (select id from data_owner where code = 'CBSGM0576');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code = 'CBSGM0585');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code = 'CBSGM0611');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code = 'CBSGM0588');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code = 'CBSGM0584');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code = 'CBSGM0617');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0394') where data_owner in (select id from data_owner where code = 'CBSGM0393');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code = 'CBSGM0005');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code = 'CBSGM1663');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code = 'CBSGM1651');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code = 'CBSGM0053');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code = 'CBSGM0015');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code = 'CBSGM0022');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code = 'CBSGM0025');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code = 'CBSGM0056');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0014') where data_owner in (select id from data_owner where code = 'CBSGM0009');
update upload set data_owner = (select id from data_owner where code = 'CBSGM0014') where data_owner in (select id from data_owner where code = 'CBSGM0017');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code = 'CBSGM0058');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code = 'CBSGM1722');
update upload set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code = 'CBSGM0079');


-- gebruiker_data_owner_rights laten wijzen naar nieuwe dataownercode


update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0361') where data_owner in (select id from data_owner where code =	'CBSGM0365');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0361') where data_owner in (select id from data_owner where code =	'CBSGM0458');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code =	'CBSGM0265');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code =	'CBSGM0282');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1930') where data_owner in (select id from data_owner where code =	'CBSGM0568');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1930') where data_owner in (select id from data_owner where code =	'CBSGM0612');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code =	'CBSGM0491');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code =	'CBSGM0643');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code =	'CBSGM0644');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code =	'CBSGM0608');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1931') where data_owner in (select id from data_owner where code =	'CBSGM0623');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0796') where data_owner in (select id from data_owner where code =	'CBSGM1671');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0828') where data_owner in (select id from data_owner where code =	'CBSGM1671');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1940') where data_owner in (select id from data_owner where code =	'CBSGM1921');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1945') where data_owner in (select id from data_owner where code =	'CBSGM0241');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code =	'CBSGM0381');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code =	'CBSGM0424');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1942') where data_owner in (select id from data_owner where code =	'CBSGM0425');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0385') where data_owner in (select id from data_owner where code =	'CBSGM0478');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code =	'CBSGM0844');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code =	'CBSGM0846');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1948') where data_owner in (select id from data_owner where code =	'CBSGM0860');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1950') where data_owner in (select id from data_owner where code =	'CBSGM0007');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1950') where data_owner in (select id from data_owner where code =	'CBSGM0048');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code =	'CBSGM0018');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code =	'CBSGM0040');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1952') where data_owner in (select id from data_owner where code =	'CBSGM1987');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code =	'CBSGM0063');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code =	'CBSGM0070');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code =	'CBSGM1908');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0080') where data_owner in (select id from data_owner where code =	'CBSGM0140');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1900') where data_owner in (select id from data_owner where code =	'CBSGM0140');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1949') where data_owner in (select id from data_owner where code =	'CBSGM0140');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0080') where data_owner in (select id from data_owner where code =	'CBSGM0081');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0299') where data_owner in (select id from data_owner where code =	'CBSGM0196');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code =	'CBSGM0951');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code =	'CBSGM0881');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1954') where data_owner in (select id from data_owner where code =	'CBSGM0962');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code =	'CBSGM0738');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code =	'CBSGM0870');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1959') where data_owner in (select id from data_owner where code =	'CBSGM0874');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code =	'CBSGM0236');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code =	'CBSGM0304');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1960') where data_owner in (select id from data_owner where code =	'CBSGM0733');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code =	'CBSGM0545');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code =	'CBSGM0707');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1961') where data_owner in (select id from data_owner where code =	'CBSGM0620');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1978') where data_owner in (select id from data_owner where code =	'CBSGM1927');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1978') where data_owner in (select id from data_owner where code =	'CBSGM0689');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0575') where data_owner in (select id from data_owner where code =	'CBSGM0576');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code =	'CBSGM0585');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code =	'CBSGM0611');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code =	'CBSGM0588');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code =	'CBSGM0584');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1963') where data_owner in (select id from data_owner where code =	'CBSGM0617');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0394') where data_owner in (select id from data_owner where code =	'CBSGM0393');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code =	'CBSGM0005');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code =	'CBSGM1663');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code =	'CBSGM1651');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1966') where data_owner in (select id from data_owner where code =	'CBSGM0053');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code =	'CBSGM0015');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code =	'CBSGM0022');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code =	'CBSGM0025');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1969') where data_owner in (select id from data_owner where code =	'CBSGM0056');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0014') where data_owner in (select id from data_owner where code =	'CBSGM0009');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM0014') where data_owner in (select id from data_owner where code =	'CBSGM0017');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code =	'CBSGM0058');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code =	'CBSGM1722');
update gebruiker_data_owner_rights set data_owner = (select id from data_owner where code = 'CBSGM1970') where data_owner in (select id from data_owner where code =	'CBSGM0079');


-- oude dataowners verwijderen

delete from data_owner where code in ('CBSGM0241','CBSGM0265','CBSGM0282','CBSGM0612','CBSGM0568','CBSGM0860','CBSGM0844','CBSGM0846','CBSGM0365','CBSGM0458','CBSGM0265','CBSGM0282','CBSGM0568','CBSGM0612','CBSGM0491','CBSGM0643','CBSGM0644','CBSGM0608','CBSGM0623','CBSGM1671','CBSGM1671','CBSGM1921','CBSGM0241','CBSGM0381','CBSGM0424','CBSGM0425','CBSGM0478','CBSGM0844','CBSGM0846','CBSGM0860','CBSGM0007','CBSGM0048','CBSGM0018','CBSGM0040','CBSGM1987','CBSGM0063','CBSGM0070','CBSGM1908','CBSGM0140','CBSGM0140','CBSGM0140','CBSGM0081','CBSGM0196','CBSGM0951','CBSGM0881','CBSGM0962','CBSGM0738','CBSGM0870','CBSGM0874','CBSGM0236','CBSGM0304','CBSGM0733','CBSGM0545','CBSGM0707','CBSGM0620','CBSGM1927','CBSGM0689','CBSGM0576','CBSGM0585','CBSGM0611','CBSGM0588','CBSGM0584','CBSGM0617','CBSGM0393','CBSGM0005','CBSGM1663','CBSGM1651','CBSGM0053','CBSGM0015','CBSGM0022','CBSGM0025','CBSGM0056','CBSGM0009','CBSGM0017','CBSGM0058','CBSGM1722','CBSGM0079');

-- View: data.v_gemeente

 DROP VIEW data.v_gemeente;
 DROP table data.gemeente;

-- insert shape 



select addgeometrycolumn('data','gemeente', 'geom_simplified', 28992, 'MULTIPOLYGON',2);

update gemeente set geom_simplified = st_multi(ST_SimplifyPreserveTopology(geom, 100));

CREATE INDEX gemeente_geom_simplified_idx
  ON gemeente
  USING gist
  (geom_simplified);


CREATE OR REPLACE VIEW data.v_gemeente AS
 SELECT g.gid,
    g.gml_id,
    g.code,
    g.gemeentena,
    g.geom_simplified AS geom,
    (( SELECT r.id AS aao
           FROM data.roadside_equipment r
             JOIN data.data_owner dao ON dao.id = r.data_owner
          WHERE ('CBSGM'::text || g.code::text) = dao.code::text
         LIMIT 1)) IS NOT NULL AS hasrseqs
   FROM data.gemeente g;

ALTER TABLE data.v_gemeente
    OWNER TO geo_ov;

