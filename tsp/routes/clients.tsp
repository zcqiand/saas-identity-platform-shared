using TypeSpec.Http;

// /clients — public client metadata; secrets and token storage never leave the IdP.
@route("/clients")
@tag("clients")
namespace Saas.Identity.Shared.Clients {
  // M04.F01 public client metadata
  @get
  @route("/{clientId}")
  op getClient(@path clientId: string): OAuthClientPublicInfo | ErrorResponse;
}
