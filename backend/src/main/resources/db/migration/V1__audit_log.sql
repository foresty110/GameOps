CREATE TABLE audit_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    admin_id      BIGINT       NOT NULL,
    action_type   VARCHAR(64)  NOT NULL,
    target_type   VARCHAR(64)  NOT NULL,
    target_id     VARCHAR(128) NULL,
    before_json   TEXT         NULL,
    after_json    TEXT         NULL,
    reason        VARCHAR(500) NULL,
    ip_address    VARCHAR(64)  NULL,
    created_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_log_admin (admin_id, created_at),
    KEY idx_audit_log_target (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
