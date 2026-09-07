// src/db/schema.ts — saas-identity-platform DB SSOT (target tenant/client/member model)
//
// The database model is intentionally aligned with the platform DDL:
// - sys_user is global; tenant_member carries the tenant-local identity.
// - oauth_client is the application boundary; tenant_application is subscription.
// - role/menu grants are relational rows, never role_ids/menu_ids arrays.
// - token rows always retain client, user and tenant context when issued.

import {
  pgTable,
  uuid,
  varchar,
  text,
  smallint,
  integer,
  boolean,
  timestamp,
  uniqueIndex,
  unique,
  index,
  primaryKey,
} from "drizzle-orm/pg-core";
import { sql } from "drizzle-orm";

const ROOT_MENU_ID = "00000000-0000-0000-0000-000000000000";

export const sysUsers = pgTable(
  "sys_user",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    username: varchar("username", { length: 64 }).notNull(),
    password: varchar("password", { length: 255 }).notNull(),
    email: varchar("email", { length: 128 }),
    mobile: varchar("mobile", { length: 32 }),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_sys_user_username").on(t.username),
    uniqueIndex("uk_sys_user_email").on(t.email),
    uniqueIndex("uk_sys_user_mobile").on(t.mobile),
  ],
);

export const tenants = pgTable(
  "tenant",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    tenantKey: varchar("tenant_key", { length: 64 }).notNull(),
    name: varchar("name", { length: 128 }).notNull(),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [uniqueIndex("uk_tenant_key").on(t.tenantKey)],
);

export const tenantMembers = pgTable(
  "tenant_member",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    tenantId: uuid("tenant_id")
      .notNull()
      .references(() => tenants.id, { onDelete: "cascade" }),
    userId: uuid("user_id")
      .notNull()
      .references(() => sysUsers.id, { onDelete: "cascade" }),
    memberName: varchar("member_name", { length: 64 }),
    isOwner: boolean("is_owner").notNull().default(false),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_tenant_user").on(t.tenantId, t.userId),
    index("idx_tenant_member_user_id").on(t.userId),
    index("idx_tenant_member_tenant_id").on(t.tenantId),
  ],
);

export const oauthClients = pgTable(
  "oauth_client",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    clientId: varchar("client_id", { length: 64 }).notNull(),
    clientSecret: varchar("client_secret", { length: 255 }).notNull(),
    clientName: varchar("client_name", { length: 128 }).notNull(),
    grantTypes: varchar("grant_types", { length: 255 }).notNull(),
    redirectUris: text("redirect_uris").notNull(),
    scopes: varchar("scopes", { length: 255 }),
    accessTokenValidity: integer("access_token_validity").notNull().default(7200),
    refreshTokenValidity: integer("refresh_token_validity").notNull().default(2592000),
    autoApprove: boolean("auto_approve").notNull().default(false),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [unique("uk_oauth_client_id").on(t.clientId)],
);

export const tenantApplications = pgTable(
  "tenant_application",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    tenantId: uuid("tenant_id")
      .notNull()
      .references(() => tenants.id, { onDelete: "cascade" }),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    status: smallint("status").notNull().default(1),
    expireTime: timestamp("expire_time", { withTimezone: true }),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_tenant_client").on(t.tenantId, t.clientId),
    index("idx_tenant_application_client_id").on(t.clientId),
  ],
);

export const oauthCodes = pgTable(
  "oauth_code",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    code: varchar("code", { length: 128 }).notNull(),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    userId: uuid("user_id")
      .notNull()
      .references(() => sysUsers.id, { onDelete: "cascade" }),
    tenantId: uuid("tenant_id")
      .notNull()
      .references(() => tenants.id, { onDelete: "cascade" }),
    redirectUri: varchar("redirect_uri", { length: 500 }),
    scope: varchar("scope", { length: 255 }),
    codeChallenge: varchar("code_challenge", { length: 128 }),
    codeChallengeMethod: varchar("code_challenge_method", { length: 16 }),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_oauth_code").on(t.code),
    index("idx_oauth_code_expires").on(t.expiresAt),
    index("idx_oauth_code_client_user_tenant").on(t.clientId, t.userId, t.tenantId),
  ],
);

export const oauthAccessTokens = pgTable(
  "oauth_access_token",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    tokenId: varchar("token_id", { length: 128 }).notNull(),
    accessToken: text("access_token").notNull(),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    userId: uuid("user_id").references(() => sysUsers.id, { onDelete: "set null" }),
    tenantId: uuid("tenant_id").references(() => tenants.id, { onDelete: "set null" }),
    scope: varchar("scope", { length: 255 }),
    tokenType: varchar("token_type", { length: 32 }).notNull().default("Bearer"),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    revoked: boolean("revoked").notNull().default(false),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_access_token_id").on(t.tokenId),
    index("idx_access_token_user_tenant").on(t.userId, t.tenantId),
    index("idx_access_token_expires").on(t.expiresAt),
  ],
);

export const oauthRefreshTokens = pgTable(
  "oauth_refresh_token",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    refreshToken: varchar("refresh_token", { length: 128 }).notNull(),
    accessTokenId: uuid("access_token_id")
      .notNull()
      .references(() => oauthAccessTokens.id, { onDelete: "cascade" }),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    userId: uuid("user_id").references(() => sysUsers.id, { onDelete: "set null" }),
    tenantId: uuid("tenant_id").references(() => tenants.id, { onDelete: "set null" }),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    revoked: boolean("revoked").notNull().default(false),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_refresh_token").on(t.refreshToken),
    index("idx_refresh_token_access_id").on(t.accessTokenId),
    index("idx_refresh_token_user_tenant").on(t.userId, t.tenantId),
  ],
);

export const sysMenus = pgTable(
  "sys_menu",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    parentId: uuid("parent_id")
      .notNull()
      .default(sql.raw(`'${ROOT_MENU_ID}'::uuid`)),
    title: varchar("title", { length: 64 }).notNull(),
    type: smallint("type").notNull(),
    path: varchar("path", { length: 255 }),
    component: varchar("component", { length: 255 }),
    perms: varchar("perms", { length: 128 }),
    icon: varchar("icon", { length: 128 }),
    sortOrder: integer("sort_order").notNull().default(0),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    index("idx_sys_menu_client_parent").on(t.clientId, t.parentId),
    index("idx_sys_menu_client_type").on(t.clientId, t.type),
  ],
);

export const sysRoles = pgTable(
  "sys_role",
  {
    id: uuid("id").primaryKey().default(sql`uuid_generate_v4()`),
    tenantId: uuid("tenant_id")
      .notNull()
      .references(() => tenants.id, { onDelete: "cascade" }),
    clientId: varchar("client_id", { length: 64 })
      .notNull()
      .references(() => oauthClients.clientId, { onDelete: "cascade" }),
    roleCode: varchar("role_code", { length: 64 }).notNull(),
    roleName: varchar("role_name", { length: 64 }).notNull(),
    description: varchar("description", { length: 255 }),
    isPreset: boolean("is_preset").notNull().default(false),
    status: smallint("status").notNull().default(1),
    createdAt: timestamp("created_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
    updatedAt: timestamp("updated_at", { withTimezone: true })
      .notNull()
      .default(sql`CURRENT_TIMESTAMP`),
  },
  (t) => [
    uniqueIndex("uk_tenant_client_role_code").on(t.tenantId, t.clientId, t.roleCode),
    index("idx_sys_role_tenant_client").on(t.tenantId, t.clientId),
  ],
);

export const sysRoleMenus = pgTable(
  "sys_role_menu",
  {
    roleId: uuid("role_id")
      .notNull()
      .references(() => sysRoles.id, { onDelete: "cascade" }),
    menuId: uuid("menu_id")
      .notNull()
      .references(() => sysMenus.id, { onDelete: "cascade" }),
  },
  (t) => [
    primaryKey({ columns: [t.roleId, t.menuId] }),
    index("idx_sys_role_menu_menu_id").on(t.menuId),
  ],
);

export const tenantMemberRoles = pgTable(
  "tenant_member_role",
  {
    memberId: uuid("member_id")
      .notNull()
      .references(() => tenantMembers.id, { onDelete: "cascade" }),
    roleId: uuid("role_id")
      .notNull()
      .references(() => sysRoles.id, { onDelete: "cascade" }),
  },
  (t) => [
    primaryKey({ columns: [t.memberId, t.roleId] }),
    index("idx_tenant_member_role_role_id").on(t.roleId),
  ],
);

export type SysUser = typeof sysUsers.$inferSelect;
export type NewSysUser = typeof sysUsers.$inferInsert;
export type Tenant = typeof tenants.$inferSelect;
export type NewTenant = typeof tenants.$inferInsert;
export type TenantMember = typeof tenantMembers.$inferSelect;
export type NewTenantMember = typeof tenantMembers.$inferInsert;
export type OAuthClient = typeof oauthClients.$inferSelect;
export type NewOAuthClient = typeof oauthClients.$inferInsert;
export type TenantApplication = typeof tenantApplications.$inferSelect;
export type NewTenantApplication = typeof tenantApplications.$inferInsert;
export type OAuthCode = typeof oauthCodes.$inferSelect;
export type NewOAuthCode = typeof oauthCodes.$inferInsert;
export type OAuthAccessToken = typeof oauthAccessTokens.$inferSelect;
export type NewOAuthAccessToken = typeof oauthAccessTokens.$inferInsert;
export type OAuthRefreshToken = typeof oauthRefreshTokens.$inferSelect;
export type NewOAuthRefreshToken = typeof oauthRefreshTokens.$inferInsert;
export type SysMenu = typeof sysMenus.$inferSelect;
export type NewSysMenu = typeof sysMenus.$inferInsert;
export type SysRole = typeof sysRoles.$inferSelect;
export type NewSysRole = typeof sysRoles.$inferInsert;
export type SysRoleMenu = typeof sysRoleMenus.$inferSelect;
export type TenantMemberRole = typeof tenantMemberRoles.$inferSelect;
