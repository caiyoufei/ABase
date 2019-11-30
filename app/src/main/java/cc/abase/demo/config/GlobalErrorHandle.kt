package cc.abase.demo.config

import cc.ab.base.ext.toast
import cc.abase.demo.R
import cc.abase.demo.component.login.LoginActivity
import cc.abase.demo.constants.ErrorCode
import com.blankj.utilcode.util.ActivityUtils

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/11/19 19:33
 */
class GlobalErrorHandle private constructor() {
  private object SingletonHolder {
    val holder = GlobalErrorHandle()
  }

  companion object {
    val instance = SingletonHolder.holder
  }

  var globalErrorCodes = mutableListOf(
      ErrorCode.NO_LOGIN//未登录
  )

  fun dealGlobalErrorCode(errorCode: Int) {
    val activity = ActivityUtils.getTopActivity()
    activity?.let { ac ->
      when (errorCode) {
        //未登录
        ErrorCode.NO_LOGIN -> {
          ac.runOnUiThread {
            ac.toast(R.string.need_login)
            LoginActivity.startActivity(ac)
          }
        }
        else -> {
        }
      }
    }
  }
}