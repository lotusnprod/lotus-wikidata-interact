COPY compound FROM '/data/compound.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY database FROM '/data/database.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY organism FROM '/data/organism.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY reference FROM '/data/reference.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY taxdb FROM '/data/taxdb.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY taxref FROM '/data/taxref.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');
COPY entry FROM '/data/entry.tsv' (FORMAT CSV, HEADER, DELIMITER E'\t');