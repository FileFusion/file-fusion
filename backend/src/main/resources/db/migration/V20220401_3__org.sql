CREATE TABLE org
(
    id                 VARCHAR(36)  NOT NULL,
    parent_id          VARCHAR(36)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    created_by         VARCHAR(36)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(36)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO org
VALUES ('de4959cd-ff2f-95b7-39e7-bc709091f3bc', 'root', 'Default', 'Default organization',
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
