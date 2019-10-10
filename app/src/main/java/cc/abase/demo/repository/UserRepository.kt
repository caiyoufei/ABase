package cc.abase.demo.repository

import android.util.Log
import cc.ab.base.utils.RxUtils
import cc.abase.demo.constants.WanAndroidUrls
import cc.abase.demo.repository.request.WanUserRequest
import cc.abase.demo.utils.MMkvUtils
import com.blankj.utilcode.util.EncryptUtils
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.rx.rxString
import io.reactivex.Single

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/10/9 21:37
 */
class UserRepository private constructor() {
  private object SingletonHolder {
    val holder = UserRepository()
  }

  companion object {
    val instance = SingletonHolder.holder
  }

  //注册
  fun register(
    username: String,
    password: String,
    repassword: String
  ): Single<String> {
    val request = WanAndroidUrls.User.REGISTER.httpPost(
      listOf(
        "username" to username,
        "password" to EncryptUtils.encryptMD5ToString(password),
        "repassword" to EncryptUtils.encryptMD5ToString(repassword)
      )
    )
    return WanUserRequest.instance.register(request)
        .compose(RxUtils.instance.rx2SchedulerHelperSDelay())
  }

  //登录
  fun login(
    username: String,
    password: String
  ): Single<String> {
    val request = WanAndroidUrls.User.REGISTER.httpPost(
        listOf(
            "username" to username,
            "password" to EncryptUtils.encryptMD5ToString(password)
        )
    )
    return WanUserRequest.instance.login(request)
        .map {
          //TODO 缓存用户信息
          it
        }
        .compose(RxUtils.instance.rx2SchedulerHelperSDelay())
  }

  //登出
  fun logOut() {
    clearUserInfo()
    val dis = WanAndroidUrls.User.LOGOUT.httpGet()
        .rxString()
        .map {
          Log.e("CASE", "退出成功:${it.component2() == null}")
        }
        .subscribe({}, {})
  }

  //======================用户登录相关信息======================//
  private var uid: Long = 0
  private var token: String? = null
  fun isLogin(): Boolean {
    if (uid == 0L) getUid()
    if (token.isNullOrBlank()) getToken()
    return uid > 0 && !token.isNullOrBlank()
  }

  fun getUid(): Long {
    uid = MMkvUtils.instance.getUid()
    return uid
  }

  fun getToken(): String? {
    token = MMkvUtils.instance.getToken()
    return token
  }

  fun clearUserInfo() {
    uid = 0
    token = null
    MMkvUtils.instance.clearUserInfo()
  }

  private fun setUid(uid: Long) {
    this.uid = uid
    MMkvUtils.instance.setUid(uid)
  }

  private fun setToken(token: String) {
    this.token = token
    MMkvUtils.instance.setToken(token)
  }
}
