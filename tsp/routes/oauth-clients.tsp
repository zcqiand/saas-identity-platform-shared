using TypeSpec.Http;

// /oauth-clients — public client metadata; secrets and token storage never leave the IdP.
@route("/oauth-clients")
@tag("oauth-clients")
namespace Saas.Identity.Shared.OAuthClients {
  // M04.F01 public client metadata
  @get
  @route("/{clientId}")
  op getOAuthClient(@path clientId: string): OAuthClientPublicInfo | ErrorResponse;
}
