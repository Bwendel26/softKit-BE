-- =====================================================================
-- V1 - Initial schema for PostgreSQL
--   - users
--   - password_reset_tokens
--   - seed data for authentication tests
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =======================
-- USERS TABLE
-- =======================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL,
    email_verified_at TIMESTAMP(3),

    password_hash VARCHAR(255) NOT NULL,
    phone_e164 VARCHAR(20),

    -- role / status como string + constraint (mapeado via @Enumerated(EnumType.STRING))
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    failed_login_attempts INTEGER NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until TIMESTAMPTZ(3),
    last_login_at TIMESTAMPTZ(3),

    created_at TIMESTAMPTZ(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMPTZ(3) NULL,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('CUSTOMER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('PENDING', 'ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_users_status ON users(status);


-- =======================
-- PASSWORD_RESET_TOKENS
-- =======================

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMPTZ(3) NOT NULL,
    used_at TIMESTAMPTZ(3),
    created_at TIMESTAMPTZ(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT uq_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_tokens_user
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);


-- =======================
-- SEED DATA (AUTH TESTS)
-- =======================
-- Passwords (raw) para referência:
-- ADMIN:   Admin@123!
-- Maria:   Maria@123!
-- João:    Joao@123!
-- Ana:     Ana@123!
-- Carlos:  Carlos@123!
-- Guest:   Guest@123!
-- Locked:  Locked@123!
-- Disabled: Disabled@123!

INSERT INTO users (
    full_name,
    email,
    password_hash,
    role,
    status,
    email_verified_at,
    failed_login_attempts,
    locked_until,
    last_login_at,
    phone_e164,
    created_at,
    updated_at
) VALUES
      (
          'Francisco Admin',
          'admin@softkit.local',
          '$2b$10$UvXMaTA8FWWUz99b/UZnxOyOQSFwUpgakYJnW98z159iac9WYNmBy',
          'ADMIN',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '60 days',
          0,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '1 day',
          '+5511999990001',
          CURRENT_TIMESTAMP(3) - INTERVAL '60 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '1 day'
      ),
      (
          'Maria Souza',
          'maria@softkit.local',
          '$2b$10$rwMT1bd2jLeApPdIqwBwd.h2m.2cuBVzx4eyppFRl076J05./zMxS',
          'CUSTOMER',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '20 days',
          0,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '2 days',
          '+5511999990002',
          CURRENT_TIMESTAMP(3) - INTERVAL '20 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '2 days'
      ),
      (
          'João Pereira',
          'joao@softkit.local',
          '$2b$10$1d62QlVAwQA5DtWhfZuc9OIe8mtQf0D3a.HdmBkg.3gXWFvutU/7S',
          'CUSTOMER',
          'PENDING',
          NULL,
          0,
          NULL,
          NULL,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '3 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '3 days'
      ),
      (
          'Ana Lima',
          'ana@softkit.local',
          '$2b$10$/e7fh8m/pEKyRH/TMqfwFuP/EynoPyDiPXHOE/5lVDNs11HAPnyGC',
          'CUSTOMER',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '10 days',
          0,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '6 hours',
          '+5511999990003',
          CURRENT_TIMESTAMP(3) - INTERVAL '10 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '6 hours'
      ),
      (
          'Carlos Silva',
          'carlos@softkit.local',
          '$2b$10$oAGsPkhCLTV2zyNLD1eDNuBUNa1yzUPuqYBpzVjECKCQrE.vGft52',
          'CUSTOMER',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '5 days',
          1,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '12 hours',
          '+5511999990004',
          CURRENT_TIMESTAMP(3) - INTERVAL '5 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '12 hours'
      ),
      (
          'Guest User',
          'guest@softkit.local',
          '$2b$10$hlnAEJ7F7Za.U1JhxHDQCu9QVP/NssVlk/rsOX7nBFoavmgSzO2iK',
          'CUSTOMER',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '1 day',
          0,
          NULL,
          NULL,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '1 day',
          CURRENT_TIMESTAMP(3) - INTERVAL '1 day'
      ),
      (
          'Locked Account',
          'locked@softkit.local',
          '$2b$10$MMzWd5H3bEfDNHZy0nW4O.UBiMq3Odc6Fe14pEVj63EeOAVpfhAO.',
          'CUSTOMER',
          'ACTIVE',
          CURRENT_TIMESTAMP(3) - INTERVAL '30 days',
          5,
          CURRENT_TIMESTAMP(3) + INTERVAL '15 minutes',
          CURRENT_TIMESTAMP(3) - INTERVAL '10 minutes',
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '30 days',
          CURRENT_TIMESTAMP(3)
      ),
      (
          'Disabled Account',
          'disabled@softkit.local',
          '$2b$10$uNi/9k7F/ErTNJNruo8O2eWOzORmoT0psUA6WYBoQv6W00TZsQECS',
          'CUSTOMER',
          'DISABLED',
          CURRENT_TIMESTAMP(3) - INTERVAL '90 days',
          0,
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '40 days',
          NULL,
          CURRENT_TIMESTAMP(3) - INTERVAL '90 days',
          CURRENT_TIMESTAMP(3) - INTERVAL '40 days'
      );
