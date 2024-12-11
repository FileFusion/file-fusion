CREATE TABLE user_info
(
    id                      VARCHAR(36)  NOT NULL,
    username                VARCHAR(100) NOT NULL,
    password                VARCHAR(100) NOT NULL,
    name                    VARCHAR(50)  NOT NULL,
    email                   VARCHAR(100) DEFAULT NULL,
    area_code               VARCHAR(10)  DEFAULT NULL,
    phone                   VARCHAR(50)  DEFAULT NULL,
    earliest_credentials    TIMESTAMP    NOT NULL,
    systemd_user            BOOLEAN      NOT NULL,
    created_by              VARCHAR(36)  NOT NULL,
    created_date            TIMESTAMP    NOT NULL,
    last_modified_by        VARCHAR(36)  DEFAULT NULL,
    last_modified_date      TIMESTAMP    DEFAULT NULL,
    non_expired             BOOLEAN      NOT NULL,
    non_locked              BOOLEAN      NOT NULL,
    credentials_non_expired BOOLEAN      NOT NULL,
    enabled                 BOOLEAN      NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_role
(
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE org_user
(
    org_id  VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (org_id, user_id)
);

INSERT INTO user_info
VALUES ('e5e4d892-63fa-278e-f3ef-5c3496e7f621', 'admin', '$2a$10$JQzT2OwiU0oVG6KJu4Gs3.viGorc0FHIv6JAnlA1KIG5uRpYwGxfC',
        'Admin', null, null, null, '2022-04-01 00:00:00', true,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null,
        true, true, true, true);

INSERT INTO user_role
VALUES ('e5e4d892-63fa-278e-f3ef-5c3496e7f621', 'a9a7b95d-6e62-4b71-a40f-de3887cdfb4a');

INSERT INTO org_user
VALUES ('de4959cd-ff2f-95b7-39e7-bc709091f3bc', 'e5e4d892-63fa-278e-f3ef-5c3496e7f621');
