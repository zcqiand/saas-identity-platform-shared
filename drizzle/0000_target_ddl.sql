CREATE TABLE IF NOT EXISTS "oauth_access_token" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"token_id" varchar(128) NOT NULL,
	"access_token" text NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"user_id" uuid,
	"tenant_id" uuid,
	"scope" varchar(255),
	"token_type" varchar(32) DEFAULT 'Bearer' NOT NULL,
	"expires_at" timestamp with time zone NOT NULL,
	"revoked" boolean DEFAULT false NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "oauth_client" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"client_secret" varchar(255) NOT NULL,
	"client_name" varchar(128) NOT NULL,
	"grant_types" varchar(255) NOT NULL,
	"redirect_uris" text NOT NULL,
	"scopes" varchar(255),
	"access_token_validity" integer DEFAULT 7200 NOT NULL,
	"refresh_token_validity" integer DEFAULT 2592000 NOT NULL,
	"auto_approve" boolean DEFAULT false NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "oauth_code" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"code" varchar(128) NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"user_id" uuid NOT NULL,
	"tenant_id" uuid NOT NULL,
	"redirect_uri" varchar(500),
	"scope" varchar(255),
	"code_challenge" varchar(128),
	"code_challenge_method" varchar(16),
	"expires_at" timestamp with time zone NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "oauth_refresh_token" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"refresh_token" varchar(128) NOT NULL,
	"access_token_id" uuid NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"user_id" uuid,
	"tenant_id" uuid,
	"expires_at" timestamp with time zone NOT NULL,
	"revoked" boolean DEFAULT false NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "sys_menu" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"parent_id" uuid DEFAULT '00000000-0000-0000-0000-000000000000'::uuid NOT NULL,
	"title" varchar(64) NOT NULL,
	"type" smallint NOT NULL,
	"path" varchar(255),
	"component" varchar(255),
	"perms" varchar(128),
	"icon" varchar(128),
	"sort_order" integer DEFAULT 0 NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "sys_role_menu" (
	"role_id" uuid NOT NULL,
	"menu_id" uuid NOT NULL,
	CONSTRAINT "sys_role_menu_role_id_menu_id_pk" PRIMARY KEY("role_id","menu_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "sys_role" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"role_code" varchar(64) NOT NULL,
	"role_name" varchar(64) NOT NULL,
	"description" varchar(255),
	"is_preset" boolean DEFAULT false NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "sys_user" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"username" varchar(64) NOT NULL,
	"password" varchar(255) NOT NULL,
	"email" varchar(128),
	"mobile" varchar(32),
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenant_application" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"client_id" varchar(64) NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"expire_time" timestamp with time zone,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenant_member_role" (
	"member_id" uuid NOT NULL,
	"role_id" uuid NOT NULL,
	CONSTRAINT "tenant_member_role_member_id_role_id_pk" PRIMARY KEY("member_id","role_id")
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenant_member" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_id" uuid NOT NULL,
	"user_id" uuid NOT NULL,
	"member_name" varchar(64),
	"is_owner" boolean DEFAULT false NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE TABLE IF NOT EXISTS "tenant" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
	"tenant_key" varchar(64) NOT NULL,
	"name" varchar(128) NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_access_token" ADD CONSTRAINT "oauth_access_token_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_access_token" ADD CONSTRAINT "oauth_access_token_user_id_sys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_access_token" ADD CONSTRAINT "oauth_access_token_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_code" ADD CONSTRAINT "oauth_code_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_code" ADD CONSTRAINT "oauth_code_user_id_sys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_code" ADD CONSTRAINT "oauth_code_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_refresh_token" ADD CONSTRAINT "oauth_refresh_token_access_token_id_oauth_access_token_id_fk" FOREIGN KEY ("access_token_id") REFERENCES "public"."oauth_access_token"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_refresh_token" ADD CONSTRAINT "oauth_refresh_token_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_refresh_token" ADD CONSTRAINT "oauth_refresh_token_user_id_sys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "oauth_refresh_token" ADD CONSTRAINT "oauth_refresh_token_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE set null ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "sys_menu" ADD CONSTRAINT "sys_menu_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "sys_role_menu" ADD CONSTRAINT "sys_role_menu_role_id_sys_role_id_fk" FOREIGN KEY ("role_id") REFERENCES "public"."sys_role"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "sys_role_menu" ADD CONSTRAINT "sys_role_menu_menu_id_sys_menu_id_fk" FOREIGN KEY ("menu_id") REFERENCES "public"."sys_menu"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "sys_role" ADD CONSTRAINT "sys_role_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "sys_role" ADD CONSTRAINT "sys_role_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_application" ADD CONSTRAINT "tenant_application_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_application" ADD CONSTRAINT "tenant_application_client_id_oauth_client_client_id_fk" FOREIGN KEY ("client_id") REFERENCES "public"."oauth_client"("client_id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_member_role" ADD CONSTRAINT "tenant_member_role_member_id_tenant_member_id_fk" FOREIGN KEY ("member_id") REFERENCES "public"."tenant_member"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_member_role" ADD CONSTRAINT "tenant_member_role_role_id_sys_role_id_fk" FOREIGN KEY ("role_id") REFERENCES "public"."sys_role"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_member" ADD CONSTRAINT "tenant_member_tenant_id_tenant_id_fk" FOREIGN KEY ("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
DO $$ BEGIN
 ALTER TABLE "tenant_member" ADD CONSTRAINT "tenant_member_user_id_sys_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."sys_user"("id") ON DELETE cascade ON UPDATE no action;
EXCEPTION
 WHEN duplicate_object THEN null;
END $$;
--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_access_token_id" ON "oauth_access_token" USING btree ("token_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_access_token_user_tenant" ON "oauth_access_token" USING btree ("user_id","tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_access_token_expires" ON "oauth_access_token" USING btree ("expires_at");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_oauth_client_id" ON "oauth_client" USING btree ("client_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_oauth_code" ON "oauth_code" USING btree ("code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_oauth_code_expires" ON "oauth_code" USING btree ("expires_at");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_oauth_code_client_user_tenant" ON "oauth_code" USING btree ("client_id","user_id","tenant_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_refresh_token" ON "oauth_refresh_token" USING btree ("refresh_token");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_refresh_token_access_id" ON "oauth_refresh_token" USING btree ("access_token_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_refresh_token_user_tenant" ON "oauth_refresh_token" USING btree ("user_id","tenant_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_sys_menu_client_parent" ON "sys_menu" USING btree ("client_id","parent_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_sys_menu_client_type" ON "sys_menu" USING btree ("client_id","type");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_sys_role_menu_menu_id" ON "sys_role_menu" USING btree ("menu_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_tenant_client_role_code" ON "sys_role" USING btree ("tenant_id","client_id","role_code");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_sys_role_tenant_client" ON "sys_role" USING btree ("tenant_id","client_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_sys_user_username" ON "sys_user" USING btree ("username");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_sys_user_email" ON "sys_user" USING btree ("email");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_sys_user_mobile" ON "sys_user" USING btree ("mobile");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_tenant_client" ON "tenant_application" USING btree ("tenant_id","client_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_tenant_application_client_id" ON "tenant_application" USING btree ("client_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_tenant_member_role_role_id" ON "tenant_member_role" USING btree ("role_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_tenant_user" ON "tenant_member" USING btree ("tenant_id","user_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_tenant_member_user_id" ON "tenant_member" USING btree ("user_id");--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "idx_tenant_member_tenant_id" ON "tenant_member" USING btree ("tenant_id");--> statement-breakpoint
CREATE UNIQUE INDEX IF NOT EXISTS "uk_tenant_key" ON "tenant" USING btree ("tenant_key");