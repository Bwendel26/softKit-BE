-- Seeds para testes de autenticação (MySQL)
-- Passwords (raw) para testes:
-- ADMIN: Admin@123!
-- Maria: Maria@123!
-- Joao:  Joao@123!
-- Ana:   Ana@123!
-- Carlos: Carlos@123!
-- Guest: Guest@123!
-- Locked: Locked@123!
-- Disabled: Disabled@123!

INSERT INTO users (
    full_name, email, password_hash,
    role, status, email_verified_at,
    failed_login_attempts, locked_until, last_login_at,
    phone_e164, created_at, updated_at
) VALUES
      (
          'Francisco Admin',
          'admin@softkit.local',
          '$2b$10$UvXMaTA8FWWUz99b/UZnxOyOQSFwUpgakYJnW98z159iac9WYNmBy',
          'ADMIN', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 60 DAY),
          0, NULL, DATE_SUB(NOW(3), INTERVAL 1 DAY),
          '+5511999990001', DATE_SUB(NOW(3), INTERVAL 60 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY)
      ),
      (
          'Maria Souza',
          'maria@softkit.local',
          '$2b$10$rwMT1bd2jLeApPdIqwBwd.h2m.2cuBVzx4eyppFRl076J05./zMxS',
          'CUSTOMER', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 20 DAY),
          0, NULL, DATE_SUB(NOW(3), INTERVAL 2 DAY),
          '+5511999990002', DATE_SUB(NOW(3), INTERVAL 20 DAY), DATE_SUB(NOW(3), INTERVAL 2 DAY)
      ),
      (
          'João Pereira',
          'joao@softkit.local',
          '$2b$10$1d62QlVAwQA5DtWhfZuc9OIe8mtQf0D3a.HdmBkg.3gXWFvutU/7S',
          'CUSTOMER', 'PENDING', NULL,
          0, NULL, NULL,
          NULL, DATE_SUB(NOW(3), INTERVAL 3 DAY), DATE_SUB(NOW(3), INTERVAL 3 DAY)
      ),
      (
          'Ana Lima',
          'ana@softkit.local',
          '$2b$10$/e7fh8m/pEKyRH/TMqfwFuP/EynoPyDiPXHOE/5lVDNs11HAPnyGC',
          'CUSTOMER', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 10 DAY),
          0, NULL, DATE_SUB(NOW(3), INTERVAL 6 HOUR),
          '+5511999990003', DATE_SUB(NOW(3), INTERVAL 10 DAY), DATE_SUB(NOW(3), INTERVAL 6 HOUR)
      ),
      (
          'Carlos Silva',
          'carlos@softkit.local',
          '$2b$10$oAGsPkhCLTV2zyNLD1eDNuBUNa1yzUPuqYBpzVjECKCQrE.vGft52',
          'CUSTOMER', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 5 DAY),
          1, NULL, DATE_SUB(NOW(3), INTERVAL 12 HOUR),
          '+5511999990004', DATE_SUB(NOW(3), INTERVAL 5 DAY), DATE_SUB(NOW(3), INTERVAL 12 HOUR)
      ),
      (
          'Guest User',
          'guest@softkit.local',
          '$2b$10$hlnAEJ7F7Za.U1JhxHDQCu9QVP/NssVlk/rsOX7nBFoavmgSzO2iK',
          'CUSTOMER', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 1 DAY),
          0, NULL, NULL,
          NULL, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY)
      ),
      (
          'Locked Account',
          'locked@softkit.local',
          '$2b$10$MMzWd5H3bEfDNHZy0nW4O.UBiMq3Odc6Fe14pEVj63EeOAVpfhAO.',
          'CUSTOMER', 'ACTIVE', DATE_SUB(NOW(3), INTERVAL 30 DAY),
          5, DATE_ADD(NOW(3), INTERVAL 15 MINUTE), DATE_SUB(NOW(3), INTERVAL 10 MINUTE),
          NULL, DATE_SUB(NOW(3), INTERVAL 30 DAY), NOW(3)
      ),
      (
          'Disabled Account',
          'disabled@softkit.local',
          '$2b$10$uNi/9k7F/ErTNJNruo8O2eWOzORmoT0psUA6WYBoQv6W00TZsQECS',
          'CUSTOMER', 'DISABLED', DATE_SUB(NOW(3), INTERVAL 90 DAY),
          0, NULL, DATE_SUB(NOW(3), INTERVAL 40 DAY),
          NULL, DATE_SUB(NOW(3), INTERVAL 90 DAY), DATE_SUB(NOW(3), INTERVAL 40 DAY)
      );
