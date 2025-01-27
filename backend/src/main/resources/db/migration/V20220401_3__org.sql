CREATE TABLE org
(
    id                 VARCHAR(26)  NOT NULL,
    parent_id          VARCHAR(26)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    created_by         VARCHAR(26)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(26)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO org
VALUES ('01JJK6FQS1E8XN0ZERMCCPC666', 'root', 'Default', 'Default organization',
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
