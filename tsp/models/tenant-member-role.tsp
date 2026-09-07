using TypeSpec.Http;

// tenant_member_role — relational member-to-role grant.
// TS model: TenantMemberRole ↔ DB table: public.tenant_member_role (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model TenantMemberRole {
  @format("uuid") memberId: string;

  @format("uuid") roleId: string;
}
