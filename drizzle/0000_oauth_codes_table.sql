CREATE TYPE "public"."api_key_status" AS ENUM('active', 'revoked', 'expired');--> statement-breakpoint
CREATE TYPE "public"."app_status" AS ENUM('active', 'disabled');--> statement-breakpoint
CREATE TYPE "public"."audit_action" AS ENUM('user_created', 'user_updated', 'user_deleted', 'role_assigned', 'role_revoked', 'login_success', 'login_failed', 'oauth_token_issued', 'api_key_created', 'api_key_revoked');--> statement-breakpoint
CREATE TYPE "public"."membership_status" AS ENUM('active', 'invited', 'removed');--> statement-breakpoint
CREATE TYPE "public"."menu_status" AS ENUM('active', 'disabled');--> statement-breakpoint
CREATE TYPE "public"."menu_type" AS ENUM('group', 'page', 'action');--> statement-breakpoint
CREATE TYPE "public"."oauth_grant_type" AS ENUM('authorization_code', 'refresh_token', 'client_credentials', 'password');--> statement-breakpoint
CREATE TYPE "public"."tenant_status" AS ENUM('active', 'suspended', 'archived');--> statement-breakpoint
CREATE TYPE "public"."user_status" AS ENUM('active', 'invited', 'suspended', 'disabled');--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "api_keys" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"name" varchar(128) NOT NULL,
	"prefix" varchar(16) NOT NULL,
	"secret_hash" varchar(255) NOT NULL,
	"status" "api_key_status" DEFAULT 'active' NOT NULL,
	"scopes" text[] DEFAULT ARRAY[]::TEXT[] NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"last_used_at" timestamp with time zone,
	"expires_at" timestamp with time zone,
	"revoked_at" timestamp with time zone,
	CONSTRAINT "api_keys_revoked_at_consistency" CHECK ((status = 'revoked' AND revoked_at IS NOT NULL) OR (status <> 'revoked'))
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "apps" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"code" varchar(64) NOT NULL,
	"name" varchar(255) NOT NULL,
	"description" text,
	"icon" varchar(64),
	"sort_order" integer DEFAULT 0 NOT NULL,
	"status" "app_status" DEFAULT 'active' NOT NULL,
	"client_id" varchar(128) NOT NULL,
	"client_secret_hash" varchar(255),
	"redirect_uris" text[] DEFAULT ARRAY[]::TEXT[] NOT NULL,
	"scopes" text[] DEFAULT ARRAY[]::TEXT[] NOT NULL,
	"grant_types" "oauth_grant_type"[] DEFAULT ARRAY[]::oauth_grant_type[] NOT NULL,
	"is_first_party" boolean DEFAULT false NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "audit_events" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"actor_user_id" uuid,
	"action" "audit_action" NOT NULL,
	"target_user_id" uuid,
	"metadata" jsonb DEFAULT '{}'::jsonb NOT NULL,
	"occurred_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "audit_events_metadata_is_object" CHECK (metadata IS NOT NULL AND jsonb_typeof(metadata) = 'object')
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "audit_retention_policies" (
	"tenant_id" uuid PRIMARY KEY NOT NULL,
	"retention_days" integer DEFAULT 90 NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "audit_retention_days_positive" CHECK (retention_days >= 1 AND retention_days <= 3650)
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "menus" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"app_id" uuid NOT NULL,
	"parent_id" uuid,
	"code" varchar(64) NOT NULL,
	"name" varchar(255) NOT NULL,
	"path" varchar(512),
	"icon" varchar(64),
	"type" "menu_type" DEFAULT 'page' NOT NULL,
	"sort_order" integer DEFAULT 0 NOT NULL,
	"status" "menu_status" DEFAULT 'active' NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "oauth_codes" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"code" varchar(255) NOT NULL,
	"grant_type" varchar(32) DEFAULT 'authorization_code' NOT NULL,
	"app_id" uuid NOT NULL,
	"user_id" uuid,
	"tenant_id" uuid NOT NULL,
	"redirect_uri" varchar(2048),
	"scope" varchar(512),
	"expires_at" timestamp with time zone NOT NULL,
	"consumed_at" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "oauth_codes_grant_type_check" CHECK (grant_type IN ('authorization_code', 'refresh_token'))
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "permissions" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"code" varchar(128) NOT NULL,
	"name" varchar(255) NOT NULL,
	"description" text,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "role_menu_grants" (
	"role_id" uuid PRIMARY KEY NOT NULL,
	"tenant_id" uuid NOT NULL,
	"menu_ids" uuid[] DEFAULT ARRAY[]::UUID[] NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "role_permissions" (
	"role_id" uuid NOT NULL,
	"permission_id" uuid NOT NULL,
	"granted_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "role_permissions_role_id_permission_id_pk" PRIMARY KEY("role_id","permission_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "roles" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"code" varchar(64) NOT NULL,
	"name" varchar(255) NOT NULL,
	"description" text,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenant_memberships" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"user_id" uuid NOT NULL,
	"tenant_id" uuid NOT NULL,
	"role_ids" uuid[] DEFAULT ARRAY[]::UUID[] NOT NULL,
	"status" "membership_status" DEFAULT 'invited' NOT NULL,
	"joined_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenants" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"code" varchar(64) NOT NULL,
	"name" varchar(255) NOT NULL,
	"status" "tenant_status" DEFAULT 'active' NOT NULL,
	"settings" jsonb DEFAULT '{}'::jsonb NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "tenants_settings_is_object" CHECK (settings IS NOT NULL AND jsonb_typeof(settings) = 'object')
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "users" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"username" varchar(64) NOT NULL,
	"email" varchar(255) NOT NULL,
	"display_name" varchar(255),
	"status" "user_status" DEFAULT 'invited' NOT NULL,
	"password_hash" varchar(255),
	"role_ids" uuid[] DEFAULT ARRAY[]::UUID[] NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT "users_email_format" CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "api_keys" ADD CONSTRAINT "api_keys_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "audit_events" ADD CONSTRAINT "audit_events_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "audit_events" ADD CONSTRAINT "audit_events_actor_user_id_users_id_fk" FOREIGN KEY ("actor_user_id") REFERENCES "public"."users"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "audit_events" ADD CONSTRAINT "audit_events_target_user_id_users_id_fk" FOREIGN KEY ("target_user_id") REFERENCES "public"."users"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "audit_retention_policies" ADD CONSTRAINT "audit_retention_policies_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "menus" ADD CONSTRAINT "menus_app_id_apps_id_fk" FOREIGN KEY ("app_id") REFERENCES "public"."apps"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "menus" ADD CONSTRAINT "menus_parent_id_menus_id_fk" FOREIGN KEY ("parent_id") REFERENCES "public"."menus"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_codes" ADD CONSTRAINT "oauth_codes_app_id_apps_id_fk" FOREIGN KEY ("app_id") REFERENCES "public"."apps"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "role_menu_grants" ADD CONSTRAINT "role_menu_grants_role_id_roles_id_fk" FOREIGN KEY ("role_id") REFERENCES "public"."roles"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "role_menu_grants" ADD CONSTRAINT "role_menu_grants_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "role_permissions" ADD CONSTRAINT "role_permissions_role_id_roles_id_fk" FOREIGN KEY ("role_id") REFERENCES "public"."roles"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "role_permissions" ADD CONSTRAINT "role_permissions_permission_id_permissions_id_fk" FOREIGN KEY ("permission_id") REFERENCES "public"."permissions"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "roles" ADD CONSTRAINT "roles_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_memberships" ADD CONSTRAINT "tenant_memberships_user_id_users_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."users"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_memberships" ADD CONSTRAINT "tenant_memberships_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "users" ADD CONSTRAINT "users_tenant_id_tenants_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenants"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "api_keys_tenant_prefix_unique" ON "api_keys" USING btree ("tenant_id","prefix");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_api_keys_tenant_id" ON "api_keys" USING btree ("tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_api_keys_status" ON "api_keys" USING btree ("status");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_api_keys_expires_at" ON "api_keys" USING btree ("expires_at");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_api_keys_prefix_global" ON "api_keys" USING btree ("prefix");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "apps_code_unique" ON "apps" USING btree ("code");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "apps_client_id_unique" ON "apps" USING btree ("client_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_audit_events_tenant_occurred" ON "audit_events" USING btree ("tenant_id","occurred_at" DESC NULLS LAST);--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_audit_events_actor" ON "audit_events" USING btree ("actor_user_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_audit_events_target" ON "audit_events" USING btree ("target_user_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_audit_events_action" ON "audit_events" USING btree ("action");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_audit_events_metadata_gin" ON "audit_events" USING gin ("metadata");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "menus_app_code_unique" ON "menus" USING btree ("app_id","code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_menus_app_id" ON "menus" USING btree ("app_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_menus_parent_id" ON "menus" USING btree ("parent_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_menus_app_type" ON "menus" USING btree ("app_id","type");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "oauth_codes_code_unique" ON "oauth_codes" USING btree ("code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_oauth_codes_app_id" ON "oauth_codes" USING btree ("app_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_oauth_codes_expires_at" ON "oauth_codes" USING btree ("expires_at");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_oauth_codes_user_id" ON "oauth_codes" USING btree ("user_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "permissions_code_unique" ON "permissions" USING btree ("code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_role_menu_grants_tenant_id" ON "role_menu_grants" USING btree ("tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_role_menu_grants_menu_ids_gin" ON "role_menu_grants" USING gin ("menu_ids");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_role_permissions_role_id" ON "role_permissions" USING btree ("role_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_role_permissions_permission_id" ON "role_permissions" USING btree ("permission_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "roles_tenant_code_unique" ON "roles" USING btree ("tenant_id","code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_roles_tenant_id" ON "roles" USING btree ("tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_roles_code_global" ON "roles" USING btree ("code");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "memberships_user_tenant_unique" ON "tenant_memberships" USING btree ("user_id","tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_memberships_user_id" ON "tenant_memberships" USING btree ("user_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_memberships_tenant_id" ON "tenant_memberships" USING btree ("tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_memberships_role_ids_gin" ON "tenant_memberships" USING gin ("role_ids");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "tenants_code_unique" ON "tenants" USING btree ("code");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "users_tenant_email_unique" ON "users" USING btree ("tenant_id","email");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "users_tenant_username_unique" ON "users" USING btree ("tenant_id","username");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_users_tenant_id" ON "users" USING btree ("tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_users_email_global" ON "users" USING btree ("email");