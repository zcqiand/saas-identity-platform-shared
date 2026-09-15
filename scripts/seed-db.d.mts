// scripts/seed-db.d.mts — seed-db.mjs 的手写类型声明（消 tsc TS7016；lab 侧同款）。
// .mjs 本体不被 tsc include（tsconfig 只收 *.ts），声明与实现需人工同步。

export interface SeedTenant {
  id: string;
  tenantKey: string;
  name: string;
  status: number | string;
  createdAt: string;
  updatedAt: string;
}

export interface SeedUser {
  id: string;
  username: string;
  email?: string | null;
  status: number | string;
  createdAt: string;
  updatedAt: string;
}

export interface SeedRole {
  id: string;
  tenantId: string;
  clientId?: string;
  roleCode: string;
  roleName: string;
  description?: string | null;
  isPreset?: boolean;
  status?: number;
  createdAt: string;
  updatedAt: string;
}

export interface SeedMembership {
  id: string;
  tenantId: string;
  userId: string;
  status: number | string;
  roleIds?: string[];
  joinedAt: string;
}

export interface SeedApp {
  id: string;
  clientId: string;
  clientSecret?: string;
  clientName?: string;
  grantTypes?: string[];
  redirectUris?: string[];
  scopes?: string[];
  isFirstParty?: boolean;
  status: number | string;
  createdAt: string;
  updatedAt: string;
}

export interface SeedMenu {
  id: string;
  clientId: string;
  parentId?: string | null;
  title: string;
  type: string;
  path?: string | null;
  icon?: string | null;
  sortOrder?: number;
  createdAt: string;
}

export interface SeedRoleMenuGrant {
  roleId: string;
  menuIds?: string[];
}

export interface SeedSet {
  tenants: SeedTenant[];
  users: SeedUser[];
  roles: SeedRole[];
  memberships: SeedMembership[];
  apps: SeedApp[];
  menus: SeedMenu[];
  roleMenuGrants: SeedRoleMenuGrant[];
}

export interface SeedSummary {
  tenant: number;
  oauth_client: number;
  sys_user: number;
  sys_role: number;
  tenant_member: number;
  tenant_member_role: number;
  tenant_application: number;
  /** 实际落库的 sys_menu 行数（fail-safe skip 不计入） */
  sys_menu: number;
  /** clientId 查不到对应 app 被 skip 的菜单行数（ADR-0019 fail-safe skip） */
  sys_menu_skipped: number;
  sys_role_menu: number;
}

/** 最小 client 结构（pg Client 的 query 子集，避免对 pg 类型的依赖） */
export interface SeedDbClient {
  query(sql: string, values?: unknown[]): Promise<unknown>;
}

/**
 * 灌库主体：TRUNCATE 后按 FK 顺序灌入，返回摘要计数。
 * clientId 查不到对应 app 的菜单行 fail-safe skip（不落库、计入 sys_menu_skipped）。
 */
export function seedDatabase(
  client: SeedDbClient,
  seeds: SeedSet,
): Promise<SeedSummary>;
