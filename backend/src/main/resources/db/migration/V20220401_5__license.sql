CREATE TABLE license
(
    id                 VARCHAR(26)  NOT NULL,
    authorized_to      VARCHAR(100) NOT NULL,
    edition            VARCHAR(50)  NOT NULL,
    start_date         TIMESTAMP   DEFAULT NULL,
    end_date           TIMESTAMP   DEFAULT NULL,
    created_by         VARCHAR(26)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(26) DEFAULT NULL,
    last_modified_date TIMESTAMP   DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO license
VALUES ('01JJK6FQS1TE4SN1ZFFXXZ35MX', 'Personal', 'Standard Edition', null, null,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
