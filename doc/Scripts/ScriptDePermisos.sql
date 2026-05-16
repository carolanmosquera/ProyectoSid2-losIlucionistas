-- Usuario administrador con todos los poderes
CREATE USER admin WITH PASSWORD '8!4O=ag7N';

-- Usuario de solo lectura
CREATE USER users WITH PASSWORD '767P!3m&e';

--crear schema para admin
CREATE SCHEMA adminSpace AUTHORIZATION admin;

-- Permisos de login
GRANT CONNECT ON DATABASE "uniPlanDataBase" TO admin, users;



-- Admin tiene todos los derechos sobre su esquema
GRANT ALL ON SCHEMA adminSpace TO admin;

-- Users solo podrá ver (USAGE) el esquema
GRANT USAGE ON SCHEMA adminSpace TO users;

-- Lo que cree admin en adminSpace, users podrá hacer SELECT
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA adminSpace
    GRANT SELECT ON TABLES TO users;

-- Otorgar SELECT sobre todas las tablas ya creadas
GRANT SELECT ON ALL TABLES IN SCHEMA adminSpace TO users;
