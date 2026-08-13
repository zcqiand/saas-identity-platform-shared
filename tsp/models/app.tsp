using TypeSpec.Http;

// App — 平台级统一实体。承担三类职责：
//   1. 业务应用（菜单承载）：M08 菜单挂在 appId 下
//   2. OAuth client：M04 OAuth 流（authorize/token）按 clientId 识别
//   3. 租户订阅：M09 菜单授权按 appCode + tenantId 维度派发
// 区别于旧 OAuthApp：本模型同时具备展示字段（icon/sortOrder/description）
// 和 OAuth 字段（clientId/redirectUris/scopes/grantTypes/isFirstParty），
// 一个 app 一份记录。
namespace Saas.Identity.Shared;

enum AppStatus {
  active,
  disabled,
}

enum OAuthGrantType {
  authorization_code,
  refresh_token,
  client_credentials,
  password,
}

model App {
  @format("uuid") id: string;

  // === 展示字段（菜单承载） ===
  @minLength(2)
  @maxLength(64)
  code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  description?: string;

  icon?: string;

  @example(0)
  sortOrder: int32;

  status: AppStatus;

  // === OAuth 集成字段 ===
  @minLength(2)
  @maxLength(128)
  clientId: string;

  clientSecret?: string;

  redirectUris: string[];

  scopes: string[];

  grantTypes: OAuthGrantType[];

  isFirstParty: boolean;

  createdAt: utcDateTime;

  updatedAt: utcDateTime;
}

model CreateAppRequest {
  @minLength(2)
  @maxLength(64)
  code: string;

  @minLength(2)
  @maxLength(255)
  name: string;

  description?: string;

  icon?: string;

  sortOrder?: int32;

  status?: AppStatus;

  @minLength(2)
  @maxLength(128)
  clientId: string;

  clientSecret?: string;

  redirectUris: string[];

  scopes?: string[];

  grantTypes?: OAuthGrantType[];

  isFirstParty?: boolean;
}

model UpdateAppRequest {
  name?: string;

  description?: string;

  icon?: string;

  sortOrder?: int32;

  status?: AppStatus;

  redirectUris?: string[];

  scopes?: string[];

  grantTypes?: OAuthGrantType[];

  isFirstParty?: boolean;
}