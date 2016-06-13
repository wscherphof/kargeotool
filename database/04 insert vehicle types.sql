
-- Afkomstig uit "TMI8 KAR meldpunten (kv 9), v8.1.0.0, release.pdf", pagina 17

insert into vehicle_type(nummer,omschrijving) values
  (0,'Gereserveerd - niet gebruiken'),
  (1,'Bus'),
  (2,'Tram'),
  (3,'Politie'),
  (4,'Brandweer'),
  (5,'Ambulance'),
  (6,'CVV'),
  (7,'Taxi'),
  (69,'Politie niet in uniform'),
  (70,'Marechaussee'),
  (71,'Hoogwaardig Openbaar Vervoer (HOV) bus'),
  (99,'Gereserveerd - niet gebruiken');

insert into vehicle_type(nummer,omschrijving)
  select generate_series(8,68), 'Gereserveerd - niet gebruiken';

insert into vehicle_type(nummer,omschrijving)
  select generate_series(72,98), 'Gereserveerd - vrij te gebruiken';


