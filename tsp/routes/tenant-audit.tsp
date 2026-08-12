using TypeSpec.Http;

// M06 — AuditEvent (tenant-scoped)
@route("/tenants/{tenantId}/audit-events")
@tag("tenant-audit")
namespace Saas.Identity.Shared.TenantAudit {
  // M06.F01.I01 列表
  @get
  op listAuditEvents(
    @path tenantId: string,
    @query page?: int32,
    @query pageSize?: int32,
    @query actorUserId?: string,
    @query action?: AuditAction,
    @query from?: utcDateTime,
    @query to?: utcDateTime,
  ): Page<AuditEvent> | ErrorResponse;

  // M06.F01.I02 按用户查
  @get
  @route("/by-user/{userId}")
  op listAuditEventsByUser(
    @path tenantId: string,
    @path userId: string,
    @query page?: int32,
    @query pageSize?: int32,
  ): Page<AuditEvent> | ErrorResponse;

  // M06.F01.I03 导出
  @post
  @route("/export")
  op exportAuditEvents(
    @path tenantId: string,
    @body body: { from: utcDateTime; to: utcDateTime; format: "csv" | "json" },
  ): { downloadUrl: string } | ErrorResponse;

  // M06.F02.I01 留存策略
  @get
  @route("/retention")
  op getRetentionPolicy(@path tenantId: string): { retentionDays: int32 } | ErrorResponse;

  @put
  @route("/retention")
  op setRetentionPolicy(
    @path tenantId: string,
    @body body: { retentionDays: int32 },
  ): { retentionDays: int32 } | ErrorResponse;
}