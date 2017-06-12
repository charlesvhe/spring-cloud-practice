INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('provider', 'mapping', '{"projectId":1}', 'r1z1', 0);
INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('provider', 'mapping', '{"projectId":2}', 'r1z2', 0);

INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('consumer', 'mapping', '{"API":"routeTest","SR":"UK"}', 'r1z1', 0);
INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('consumer', 'mapping', '{"API":"routeTest","SR":"CN"}', 'r1z2', 0);

INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('consumer', 'mapping', '{"API":"routeTestOther","SR":"UK"}', 'r1z2', 0);
INSERT INTO rule (service, strategy, content, zone, rank)
    VALUES('consumer', 'mapping', '{"API":"routeTestOther","SR":"CN"}', 'r1z1', 0);
