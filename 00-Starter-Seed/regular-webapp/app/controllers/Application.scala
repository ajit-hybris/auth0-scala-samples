package controllers

import javax.inject.Inject
import play.api.cache._
import play.api.mvc.Action
import play.api.mvc.Controller
import helpers.Auth0Config
import java.security.SecureRandom
import java.math.BigInteger


class Application @Inject() (cache: CacheApi) extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def login = Action {
    val config = Auth0Config.get()
    // Generate random state parameter
    object RandomUtil {
      private val random = new SecureRandom()

      def alphanumeric(nrChars: Int = 24): String = {
        new BigInteger(nrChars * 5, random).toString(32)
      }
    }
    val state = RandomUtil.alphanumeric()
    var audience = config.audience
    cache.set("state", state)
    if (config.audience == ""){
      audience = String.format("https://%s/userinfo",config.domain)
    }

    val query = String.format(
      "authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=openid profile&audience=%s&state=%s",
      config.clientId,
      config.callbackURL,
      audience,
      state
    )
    Redirect(String.format("https://%s/%s",config.domain,query))
  }

  def logout = Action {
    val config = Auth0Config.get()
    Redirect(String.format(
      "https://%s/v2/logout?client_id=%s&returnTo=http://localhost:9000",
      config.domain,
      config.clientId)
    ).withNewSession
  }
}