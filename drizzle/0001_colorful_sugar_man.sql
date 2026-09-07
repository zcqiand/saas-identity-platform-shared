DROP INDEX IF EXISTS "uk_oauth_client_id";--> statement-breakpoint
ALTER TABLE "oauth_client" ADD CONSTRAINT "uk_oauth_client_id" UNIQUE("client_id");