package cc.ab.base.startup

import android.app.Application
import android.content.Context
import cc.ab.base.ext.logI
import cc.ab.base.utils.IMMLeaks
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

/**
 * @Description 输入法内存泄漏
 * @Author：Khaos
 * @Date：2021/1/15
 * @Time：16:45
 */
class ImmInit : AndroidStartup<Int>() {
  //<editor-fold defaultstate="collapsed" desc="初始化线程问题">
  //create()方法调时所在的线程：如果callCreateOnMainThread返回true，则表示在主线程中初始化，会导致waitOnMainThread返回值失效；当返回false时，才会判断waitOnMainThread的返回值
  override fun callCreateOnMainThread(): Boolean = false

  //是否需要在主线程进行等待其完成:如果返回true，将在主线程等待，并且阻塞主线程
  override fun waitOnMainThread(): Boolean = false
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化">
  override fun create(context: Context): Int {
    IMMLeaks.fixFocusedViewLeak(context.applicationContext as Application)
    "初始化完成".logI()
    return 0
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="依赖">
  override fun dependencies(): List<Class<out Startup<*>>> {
    return mutableListOf(TimberInit::class.java)
  }
  //</editor-fold>
}