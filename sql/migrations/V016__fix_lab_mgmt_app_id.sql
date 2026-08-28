-- V016__fix_lab_mgmt_app_id.sql
-- 修复 V015 menus_app_fk 外键违规（2026-08-29 saas-nextjs v0.7.49 部署事故）。
--
-- 根因：线上 saas_prod 的 apps 表 lab-management 行是早期运行时 seed
-- （saas-nextjs src/seeds/apps.json）灌的 —— id 是随机 UUID（如 5b6a189b-...），
-- V014 收敛 client_id='11111111-...' 时该行已存在，ON CONFLICT (client_id)
-- DO NOTHING 把 V014 的 INSERT（含 id='11111111-...'）整个跳过 → 表里没有
-- id='11111111-...' 的行 → V015 的 menus.app_id FK 炸。
--
-- V014 注释的假设「json seed 的 clientId 也是同一 UUID」只对了 client_id，
-- 漏了 id —— 历史 json seed 灌库时 id 是随机生成的。
--
-- 修复（幂等，可重跑；利用 menus_app_fk 的 ON DELETE CASCADE 反向清理）：
--   1. DELETE 旧随机 id 的 lab-management app 行 —— CASCADE 连带删掉挂它
--      的旧运行时 seed menus（27 条，与 V015 新树同源重复）
--   2. INSERT V014 同款标准行（id=client_id='11111111-...'）
--   3. V015 由 sync-db 重放（其事务在事故中回滚、tracking 未记录，
--      本文件应用后条件满足，新树 menus/role/grants 全套灌入）

-- 1. 删旧 id 的 lab-management app（menus CASCADE 跟走；无其他子表引用 apps.id）
DELETE FROM apps
WHERE code = 'lab-management'
  AND id <> '11111111-1111-1111-1111-111111111111';

-- 2. 灌标准行（镜像 V014 的 INSERT，防 V014 因 tracking 已记录不会重跑）
INSERT INTO apps (
    id, code, name, description, icon, sort_order, status,
    client_id, client_secret_hash, redirect_uris, scopes, grant_types,
    is_first_party, created_at, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'lab-management',
    '建筑工程实验室管理系统',
    'lab-mgmt OAuth client — 3 个 saas 后端共用同一 app.id (= client_id = UUID)',
    'flask',
    100,
    'active',
    '11111111-1111-1111-1111-111111111111',
    'lab-mgmt-secret',
    ARRAY[
        'https://lab-vue.xiangru.uk/login',
        'https://lab-react.xiangru.uk/login',
        'https://lab-nextjs.xiangru.uk/login',
        'http://localhost:5173/login',
        'http://localhost:5174/login',
        'http://localhost:3001/callback'
    ]::TEXT[],
    ARRAY['lab.read', 'lab.write']::TEXT[],
    ARRAY['authorization_code', 'refresh_token']::oauth_grant_type[],
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (client_id) DO NOTHING;
