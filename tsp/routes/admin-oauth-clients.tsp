using TypeSpec.Http;

// M04 — platform-level oauth_client administration.
@route("/admin/oauth-clients")
@tag("admin-oauth-clients")
namespace Saas.Identity.Shared.AdminOAuthClients {
  // M04.F01.I01 list clients
  @get
  op listClients(@query page?: int32, @query pageSize?: int32): Page<OAuthClient> | ErrorResponse;

  // M04.F01.I02 create client
  @post
  op createClient(@body body: CreateOAuthClientRequest): OAuthClient | ErrorResponse;

  // M04.F01.I03 client detail
  @get
  @route("/{clientId}")
  op getClient(@path clientId: string): OAuthClient | ErrorResponse;

  // M04.F01.I04 update client
  @patch
  @route("/{clientId}")
  op updateClient(@path clientId: string, @body body: UpdateOAuthClientRequest): OAuthClient | ErrorResponse;

  // M04.F01.I05 delete client
  @delete
  @route("/{clientId}")
  op deleteClient(@path clientId: string): void | ErrorResponse;

  // M04.F02.I06 enable/disable client
  @patch
  @route("/{clientId}/status")
  op setClientStatus(
    @path clientId: string,
    @body body: { status: int32 },
  ): OAuthClient | ErrorResponse;
}
