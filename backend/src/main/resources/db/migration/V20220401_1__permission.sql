CREATE TABLE permission
(
    id                 VARCHAR(36)  NOT NULL,
    parent_id          VARCHAR(36)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    basics             BOOLEAN      NOT NULL,
    created_by         VARCHAR(36)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(36)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO permission
VALUES ('user', 'root', 'User', 'User', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:read', 'user', 'User read', 'Read current user info', true,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:edit', 'user', 'User edit', 'Edit current user info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:change_password', 'user', 'User change password', 'Change current user password', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('license', 'root', 'License', 'License', true,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('license:read', 'license', 'License read', 'Read current license info', true,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('org', 'root', 'Organization', 'Organization', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:read', 'org', 'Organization read', 'Read organization info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:edit', 'org', 'Organization edit', 'Edit organization info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:add', 'org', 'Organization add', 'Add organization', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:delete', 'org', 'Organization delete', 'Delete organization', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('user_management', 'root', 'User management', 'User management', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:read', 'user_management', 'User management read', 'Read all user info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:edit', 'user_management', 'User management edit', 'Edit all user info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:add', 'user_management', 'User management add', 'Add user', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:delete', 'user_management', 'User management delete', 'Delete user', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('role', 'root', 'Role', 'Role', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:read', 'role', 'Role read', 'Read role info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:edit', 'role', 'Role edit', 'Edit role info', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:add', 'role', 'Role add', 'Add role', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:delete', 'role', 'Role delete', 'Delete role', false,
        'e5e4d892-63fa-278e-f3ef-5c3496e7f621', '2022-04-01 00:00:00', null, null);
