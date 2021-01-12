package cc.abase.demo.utils

import android.app.Application
import cc.ab.base.ext.xmlToString
import cc.abase.demo.BuildConfig
import cc.abase.demo.R
import cc.abase.demo.config.UserManager
import cc.abase.demo.constants.ApiUrl
import com.blankj.utilcode.util.AppUtils
import com.snail.antifake.jni.EmulatorDetectUtil
import com.tencent.bugly.crashreport.CrashReport

/**
 * Description:
 * @author: CASE
 * @date: 2019/11/6 17:41
 */
object BuglyManager {
  //初始化bugly
  fun initBugly(application: Application) { //bugly异常检测
    //初始化key
    CrashReport.initCrashReport(application, R.string.bugly_app_id.xmlToString(), BuildConfig.DEBUG)
    //上报APP信息
    CrashReport.putUserData(application.applicationContext, "Emulator", EmulatorDetectUtil.isEmulator(application).toString())
    CrashReport.putUserData(application.applicationContext, "Release", "${BuildConfig.APP_IS_RELEASE}")
    CrashReport.putUserData(application.applicationContext, "BuildTime", R.string.build_time.xmlToString())
    CrashReport.putUserData(application.applicationContext, "VersionName", AppUtils.getAppVersionName())
    CrashReport.putUserData(application.applicationContext, "VersionCode", "${AppUtils.getAppVersionCode()}")
    CrashReport.putUserData(application.applicationContext, "BaseUrl", ApiUrl.appBaseUrl)
    //用户信息
    setBuglyUserInfo()
  }

  //给bugly设置用户信息
  private fun setBuglyUserInfo() {
    val uid = UserManager.getUid()
    //上报用户id
    if (uid > 0) CrashReport.setUserId(uid.toString())
  }

  //上报需要统计的异常
  fun reportException(throwable: Throwable?) {
    throwable?.let { CrashReport.postCatchedException(it) }
  }

  //重新登录后的用户信息
  fun updateUserInfo() {
    setBuglyUserInfo()
  }
}