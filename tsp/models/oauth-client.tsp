using TypeSpec.Http;

// oauth_client — application/client boundary shared by OAuth and client menus.
// TS model: OAuthClient ↔ DB table: public.oauth_client (ADR-0025 schema-first)
//
// tenant_application — tenant subscription to an OAuth client.
// TS model: TenantApplication ↔ DB table: public.tenant_application (ADR-0025 schema-first)
namespace Saas.Identity.Shared;

model OAuthClient {
  @format("uuid") id: string;

  clientId: string;

  clientName: string;

  grantTypes: string;

  redirectUris: string;

  scopes?: string;

  accessTokenValidity: int32;

  refreshTokenValidity: int32;

  autoApprove: boolean;

  status: int32;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateOAuthClientRequest {
  clientId: string;

  clientName: string;

  clientSecret: string;

  grantTypes: string;

  redirectUris: string;

  scopes?: string;

  accessTokenValidity?: int32;

  refreshTokenValidity?: int32;

  autoApprove?: boolean;
}

model UpdateOAuthClientRequest {
  clientName?: string;

  grantTypes?: string;

  redirectUris?: string;

  scopes?: string;

  accessTokenValidity?: int32;

  refreshTokenValidity?: int32;

  autoApprove?: boolean;

  status?: int32;
}

model OAuthClientPublicInfo {
  clientId: string;

  clientName: string;

  status: int32;
}

model TenantApplication {
  @format("uuid") id: string;

  @format("uuid") tenantId: string;

  clientId: string;

  status: int32;

  expireTime?: utcDateTime;

  createdAt: utcDateTime;
}

model SubscribeTenantApplicationRequest {
  clientId: string;

  expireTime?: utcDateTime;
}

model UpdateTenantApplicationRequest {
  status: int32;

  expireTime?: utcDateTime;
}
