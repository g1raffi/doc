CREATE TABLE IF NOT EXISTS jsondata
    (
    id      uuid PRIMARY KEY,
    json    text
    );

INSERT INTO jsondata (id, json) VALUES ('21c23437-89d1-4774-bbbe-c286fb7c3afd', 'teststring');