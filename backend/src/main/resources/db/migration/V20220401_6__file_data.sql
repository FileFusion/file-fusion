CREATE TABLE file_data
(
    id                      VARCHAR(26)   NOT NULL,
    path                    VARCHAR(4096) NOT NULL,
    name                    VARCHAR(255)  NOT NULL,
    type                    VARCHAR(10)   NOT NULL,
    mime_type               VARCHAR(100) DEFAULT NULL,
    size                    BIGINT        NOT NULL,
    encrypted               BOOLEAN       NOT NULL,
    file_last_modified_date TIMESTAMP    DEFAULT NULL,
    created_by              VARCHAR(26)   NOT NULL,
    created_date            TIMESTAMP     NOT NULL,
    last_modified_by        VARCHAR(26)  DEFAULT NULL,
    last_modified_date      TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (id)
);
