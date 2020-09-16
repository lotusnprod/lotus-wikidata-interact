CREATE TABLE compound
(
    id       INT PRIMARY KEY,
    inchi    TEXT,
    inchikey VARCHAR(32),
    smiles   TEXT
);

CREATE TABLE database
(
    id   INT PRIMARY KEY,
    name VARCHAR(5)
);


CREATE TABLE reference
(
    id INT PRIMARY KEY,
    doi TEXT,
    pmid TEXT,
    pmcid TEXT
);

CREATE TABLE organism
(
    id   INT PRIMARY KEY,
    name TEXT
);

CREATE TABLE taxdb
(
    id   INT PRIMARY KEY,
    name TEXT
);


CREATE TABLE taxref
(
    id INT PRIMARY KEY,
    organism_id INT REFERENCES organism (id),
    taxdb_id INT REFERENCES taxdb (id),
    tax_id TEXT
);

CREATE TABLE entry
(
    id           INT PRIMARY KEY,
    organism_id  INT REFERENCES organism (id),
    reference_id INT REFERENCES reference (id),
    compound_id  INT REFERENCES compound (id),
    database_id  INT REFERENCES database (id)
);
