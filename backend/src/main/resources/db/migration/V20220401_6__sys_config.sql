CREATE TABLE sys_config
(
    id                 VARCHAR(26)  NOT NULL,
    config_key         VARCHAR(100) NOT NULL,
    config_value       VARCHAR(500) NOT NULL,
    description        VARCHAR(500) DEFAULT NULL,
    created_by         VARCHAR(26)  NOT NULL,
    created_date       TIMESTAMP    NOT NULL,
    last_modified_by   VARCHAR(26)  DEFAULT NULL,
    last_modified_date TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO sys_config
VALUES ('01JN52YTVGPK75PJ0X4ZP5CG04', 'RECYCLE_BIN', 'true', 'Whether to enable the recycle bin',
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);

INSERT INTO sys_config
VALUES ('01JN52YTVGPJPS10Z0GKT1R937', 'RECYCLE_BIN_RETENTION_TIME', '30', 'Recycle bin retention time (in days)',
        '01JJK6FQS0K3N6K4JAEAP5ZC7P', '2022-04-01 00:00:00', null, null);
