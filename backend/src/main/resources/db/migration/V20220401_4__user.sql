CREATE TABLE user_info
(
    id                      VARCHAR(26)  NOT NULL,
    username                VARCHAR(100) NOT NULL,
    password                VARCHAR(100) NOT NULL,
    name                    VARCHAR(50)  NOT NULL,
    email                   VARCHAR(100) DEFAULT NULL,
    area_code               VARCHAR(10)  DEFAULT NULL,
    phone                   VARCHAR(50)  DEFAULT NULL,
    earliest_credentials    TIMESTAMP    NOT NULL,
    systemd_user            BOOLEAN      NOT NULL,
    created_by              VARCHAR(26)  NOT NULL,
    created_date            TIMESTAMP    NOT NULL,
    last_modified_by        VARCHAR(26)  DEFAULT NULL,
    last_modified_date      TIMESTAMP    DEFAULT NULL,
    non_expired             BOOLEAN      NOT NULL,
    non_locked              BOOLEAN      NOT NULL,
    credentials_non_expired BOOLEAN      NOT NULL,
    enabled                 BOOLEAN      NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_role
(
    user_id VARCHAR(26) NOT NULL,
    role_id VARCHAR(26) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE org_user
(
    org_id  VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    PRIMARY KEY (org_id, user_id)
);

INSERT INTO user_info
VALUES ('01JJK6FQS0K3N6K4JAEAP5ZC7P', 'admin', '$2a$10$Ay1dB0oHBHdopm4YFJpVUu7N9QSumH2eTvSj7lONSsBcTgHhZ.C0W',
        'Admin', null, null, null, '2022-04-01 00:00:00', true,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null,
        true, true, true, true);

INSERT INTO user_role
VALUES ('01JJK6FQS0K3N6K4JAEAP5ZC7P', '01JJK6FQS1BSXW6VBVS1ZXGT0W');

INSERT INTO org_user
VALUES ('01JJK6FQS1E8XN0ZERMCCPC666', '01JJK6FQS0K3N6K4JAEAP5ZC7P');
