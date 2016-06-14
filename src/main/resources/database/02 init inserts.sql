INSERT INTO role (id, role) VALUES (1, 'beheerder');
INSERT INTO role (id, role) VALUES (2, 'gebruiker');
INSERT INTO gebruiker (id, username, passwordsalt, passwordhash, fullname, email, phone, "position") VALUES (1, 'beheerder', '6d692e72', 'c45ef702807b9295028d24c9a6dfb79b28dd03d6', 'beheerder', 'beheerder@beheerder.nl', NULL, NULL);
INSERT INTO gebruiker_roles (gebruiker, role) VALUES (1, 1);
INSERT INTO gebruiker_roles (gebruiker, role) VALUES (1, 2);


select setval('gebruiker_id_seq', (select max(id) from gebruiker)); 
