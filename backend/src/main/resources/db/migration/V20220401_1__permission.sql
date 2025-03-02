CREATE TABLE permission
(
    id                 VARCHAR(100) NOT NULL,
    parent_id          VARCHAR(26)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    basics             BOOLEAN      NOT NULL,
    created_by         VARCHAR(26)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(26)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO permission
VALUES ('user', 'root', 'User', 'User', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:read', 'user', 'User read', 'Read user', true,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:edit', 'user', 'User edit', 'Edit user', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user:change_password', 'user', 'User change password', 'Change user password', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('license', 'root', 'License', 'License', true,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('license:read', 'license', 'License read', 'Read license', true,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('org', 'root', 'Organization', 'Organization', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:read', 'org', 'Organization read', 'Read organization', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:edit', 'org', 'Organization edit', 'Edit organization', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:add', 'org', 'Organization add', 'Add organization', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('org:delete', 'org', 'Organization delete', 'Delete organization', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('user_management', 'root', 'User management', 'User management', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:read', 'user_management', 'User read', 'Read user', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:edit', 'user_management', 'User edit', 'Edit user', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:add', 'user_management', 'User add', 'Add user', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('user_management:delete', 'user_management', 'User delete', 'Delete user', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('role', 'root', 'Role', 'Role', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:read', 'role', 'Role read', 'Read role', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:edit', 'role', 'Role edit', 'Edit role', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:add', 'role', 'Role add', 'Add role', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('role:delete', 'role', 'Role delete', 'Delete role', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('sys_config', 'root', 'Sys config', 'Sys config', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('sys_config:recycle_bin_read', 'sys_config', 'Sys recycle bin config read', 'Read sys recycle bin config',
        false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('sys_config:recycle_bin_edit', 'sys_config', 'Sys recycle bin config edit', 'Edit sys recycle bin config',
        false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('personal_file', 'root', 'Personal file', 'Personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:read', 'personal_file', 'Personal file read', 'Read personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:edit', 'personal_file', 'Personal file edit', 'Edit personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:delete', 'personal_file', 'Personal file delete', 'Delete personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:upload', 'personal_file', 'Personal file upload', 'Upload personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:download', 'personal_file', 'Personal file download', 'Download personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('personal_file:preview', 'personal_file', 'Personal file preview', 'Preview personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO permission
VALUES ('recycle_bin_file', 'root', 'Recycle bin file', 'Personal file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('recycle_bin_file:read', 'recycle_bin_file', 'Recycle bin file read', 'Read recycle bin file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('recycle_bin_file:delete', 'recycle_bin_file', 'Recycle bin file delete', 'Delete recycle bin file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('recycle_bin_file:restore', 'recycle_bin_file', 'Recycle bin file restore', 'Restore recycle bin file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
INSERT INTO permission
VALUES ('recycle_bin_file:preview', 'recycle_bin_file', 'Recycle bin file preview', 'Preview recycle bin file', false,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
