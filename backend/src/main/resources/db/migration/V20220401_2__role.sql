CREATE TABLE role
(
    id                 VARCHAR(36)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    system_role        BOOLEAN      NOT NULL,
    created_by         VARCHAR(36)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(36)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE role_permission
(
    role_id       VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO role
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'Admin', 'Have all permissions', true,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);

INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user:edit');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user:change_password');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'org');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'org:read');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'org:edit');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'org:add');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'org:delete');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user_management');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user_management:read');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user_management:edit');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user_management:add');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'user_management:delete');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'role');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'role:read');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'role:edit');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'role:add');
INSERT INTO role_permission
VALUES ('a9a7b95d-6e62-4b71-a40f-de3887cdfb4a', 'role:delete');
