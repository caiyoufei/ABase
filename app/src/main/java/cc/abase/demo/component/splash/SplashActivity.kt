package cc.abase.demo.component.splash

import android.Manifest
import android.content.Intent
import android.util.Log
import cc.ab.base.ext.*
import cc.abase.demo.R
import cc.abase.demo.component.comm.CommActivity
import cc.abase.demo.component.login.LoginActivity
import cc.abase.demo.component.main.MainActivity
import cc.abase.demo.constants.ImageUrls
import cc.abase.demo.fuel.repository.UserRepositoryFuel
import cc.abase.demo.utils.MMkvUtils
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import me.panpf.sketch.Sketch

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/10/8 10:03
 */
class SplashActivity : CommActivity() {
  //一个小时变一张图
  private val randomImg = TimeUtils.millis2String(System.currentTimeMillis())
    .split(" ")[1].split(":")[0].toInt()
  //倒计时3秒
  private val count = 3L
  //倒计时
  private var launchJob: Job? = null
  //是否有SD卡读写权限
  private var hasSDPermission: Boolean? = null
  //倒计时是否结束
  private var countDownFinish: Boolean? = null
  //是否需要关闭页面
  private var hasFinish = false

  //不设置状态栏填充，即显示全屏
  override fun fillStatus() = false

  //状态栏透明
  override fun initStatus() {
    immersionBar { statusBarDarkFont(false) }
  }

  override fun layoutResId() = R.layout.activity_splash

  override fun initView() {
    hasFinish = checkReOpenHome()
    if (hasFinish) return
    launchJob?.cancel()
    //页面无缝过渡后重置背景，不然会导致页面显示出现问题。主要解决由于window背景设置后的一些问题
    window.setBackgroundDrawable(null)
    //有尺寸了才开始计时
    splashTime?.post {
      //使用协程进行倒计时
      launchJob = GlobalScope.launch(Dispatchers.Main) {
        for (i in count.toInt() downTo 1) {
          splashTime?.text = i.toString()
          delay(1000)
        }
        countDownFinish = true
        goNextPage()
      }
    }
  }

  override fun initData() {
    if (hasFinish) return
    loadData()
    if (PermissionUtils.isGranted(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
      )
    ) {
      hasSDPermission = true
      goNextPage()
    } else {
      PermissionUtils.permission(PermissionConstants.STORAGE)
        .callback(object : PermissionUtils.SimpleCallback {
          //权限允许
          override fun onGranted() {
            Log.e("CASE", "有SD卡读写权限:${PermissionUtils.isGranted(PermissionConstants.STORAGE)}")
            hasSDPermission = true
            goNextPage()
          }

          //权限拒绝
          override fun onDenied() {
            mContext.toast("没有SD卡权限,不能使用APP")
            hasSDPermission = false
            goNextPage()
          }
        })
        .request()
    }
  }

  //https://www.cnblogs.com/xqz0618/p/thistaskroot.html
  private fun checkReOpenHome(): Boolean {
    // 避免从桌面启动程序后，会重新实例化入口类的activity
    if (!this.isTaskRoot && intent != null // 判断当前activity是不是所在任务栈的根
      && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
      && Intent.ACTION_MAIN == intent.action
    ) {
      finish()
      return true
    }
    return false
  }

  //打开下个页面
  private fun goNextPage() {
    if (hasSDPermission == null) return
    if (countDownFinish != true) return
    if (hasSDPermission == true) {
      when {
        //是否引导
        MMkvUtils.instance.getNeedGuide() -> GuideActivity.startActivity(mContext)
        //是否登录
        UserRepositoryFuel.instance.isLogin() -> MainActivity.startActivity(mContext)
        //没有其他需要，进入主页
        else -> LoginActivity.startActivity(mContext)
      }
    }
    finish()
  }

  /**
   * 流程：默认显示的window背景
   * 1.有当前缓存图片，则直接展示
   * 2.没有缓存图片：
   *    A.有上次缓存图片，显示缓存并预加载新图
   *    B.没有任何缓存，显示默认并进行预加载
   */
  private fun loadData() {
    //图片地址
    val url = ImageUrls.instance.getRandomImgUrl(randomImg)
    //判断是否存在缓存图片
    val cacheFile = splashCover.getCacheFile(url)
    //缓存图片存在
    if (cacheFile?.exists() == true) {
      splashCover.load(cacheFile)
    } else {//加载网络图片
      splashCover.gone()
      Sketch.with(Utils.getApp())
        .download(url, null)
        .commit()
    }
  }

  override fun finish() {
    launchJob?.cancel()
    super.finish()
  }
}