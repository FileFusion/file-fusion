CREATE TABLE role
(
    id                 VARCHAR(26)  NOT NULL,
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    system_role        BOOLEAN      NOT NULL,
    created_by         VARCHAR(26)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(26)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE role_permission
(
    role_id       VARCHAR(26)  NOT NULL,
    permission_id VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO role
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'Admin', 'Have all permissions', true,
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user:edit');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user:change_password');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'org');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'org:read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'org:edit');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'org:add');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'org:delete');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user_management');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user_management:read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user_management:edit');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user_management:add');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'user_management:delete');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'role');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'role:read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'role:edit');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'role:add');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'role:delete');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'sys_config');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'sys_config:recycle_bin_read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'sys_config:recycle_bin_edit');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:rename');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:share');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:move');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:delete');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:upload');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:download');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'personal_file:preview');

INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'recycle_bin_file');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'recycle_bin_file:read');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'recycle_bin_file:delete');
INSERT INTO role_permission
VALUES ('01JJK6FQS1BSXW6VBVS1ZXGT0W', 'recycle_bin_file:restore');
