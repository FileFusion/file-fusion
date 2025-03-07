CREATE TABLE file_data
(
    id                      VARCHAR(26)   NOT NULL,
    user_id                 VARCHAR(26)   NOT NULL,
    parent_id               VARCHAR(26)   NOT NULL,
    name                    VARCHAR(255)  NOT NULL,
    path                    VARCHAR(4096) NOT NULL,
    hash_value              VARCHAR(32)  DEFAULT NULL,
    mime_type               VARCHAR(100) DEFAULT NULL,
    size                    BIGINT        NOT NULL,
    encrypted               BOOLEAN       NOT NULL,
    file_last_modified_date TIMESTAMP    DEFAULT NULL,
    deleted                 BOOLEAN       NOT NULL,
    deleted_date            TIMESTAMP    DEFAULT NULL,
    created_by              VARCHAR(26)   NOT NULL,
    created_date            TIMESTAMP     NOT NULL,
    last_modified_by        VARCHAR(26)  DEFAULT NULL,
    last_modified_date      TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);
