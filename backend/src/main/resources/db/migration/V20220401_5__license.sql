CREATE TABLE license
(
    id                 VARCHAR(36)  NOT NULL,
    authorized_to      VARCHAR(100) NOT NULL,
    workflow_quantity  INTEGER      NOT NULL,
    edition            VARCHAR(50)  NOT NULL,
    start_date         TIMESTAMP   DEFAULT NULL,
    end_date           TIMESTAMP   DEFAULT NULL,
    created_by         VARCHAR(36)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(36) DEFAULT NULL,
    last_modified_date TIMESTAMP   DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO license
VALUES ('aebfcd44-5be8-3668-b9d4-e46700dde9b2', 'Personal', 100, 'Standard Edition', null, null,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
