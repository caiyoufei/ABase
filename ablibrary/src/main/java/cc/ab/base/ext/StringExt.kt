package cc.ab.base.ext

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import coil.util.CoilUtils
import com.blankj.utilcode.util.*
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber
import java.io.File
import java.util.Locale
import java.util.regex.Pattern

/**
 * Author:Khaos
 * Date:2020-9-28
 * Time:19:01
 */
inline fun String?.logE() {
  if (!this.isNullOrBlank()) {
    Timber.e("Khaos-$this")
  }
}

inline fun String?.logW() {
  if (!this.isNullOrBlank()) {
    Timber.w("Khaos-$this")
  }
}

inline fun String?.logI() {
  if (!this.isNullOrBlank()) {
    Timber.i("Khaos-$this")
  }
}

inline fun String?.logD() {
  if (!this.isNullOrBlank()) {
    Timber.d("Khaos-$this")
  }
}

inline fun CharSequence?.toast() {
  if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(this)
  }
}

inline fun CharSequence?.toastLong() {
  if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
    ToastUtils.make().setDurationIsLong(true).setGravity(Gravity.CENTER, 0, 0).show(this)
  }
}

fun String?.isNetImageUrl(): Boolean {
  return if (this.isNullOrBlank()) {
    false
  } else if (!this.startsWith("http", true)) {
    false
  } else {
    Pattern.compile(".*?(gif|jpeg|png|jpg|bmp)").matcher(this.toLowerCase(Locale.getDefault())).matches()
  }
}

fun String?.isVideoUrl(): Boolean {
  return if (this.isNullOrBlank()) {
    false
  } else if (!this.toLowerCase(Locale.getDefault()).startsWith("http", true)) {
    false
  } else {
    Pattern.compile(".*?(avi|rmvb|rm|asf|divx|mpg|mpeg|mpe|wmv|mp4|mkv|vob)")
      .matcher(this.toLowerCase(Locale.getDefault())).matches()
  }
}

fun String?.isLiveUrl(): Boolean {
  return if (this.isNullOrBlank()) {
    false
  } else {
    this.toLowerCase(Locale.getDefault()).run {
      startsWith("rtmp") || startsWith("rtsp")
    }
  }
}

//文件目录转file
fun String?.toFile(): File? {
  if (this != null) {
    return if (this.startsWith("http", true)) null else {
      val f = File(this)
      if (f.exists()) f else UriUtils.uri2File(Uri.parse(this))
    }
  }
  return null
}

//Coil获取缓存图片文件
fun String?.getCoilCacheFile(): File? {
  return this?.toFile() ?: this?.toHttpUrlOrNull()?.let { u ->
    CoilUtils.createDefaultCache(Utils.getApp()).directory.listFiles()?.lastOrNull { it.name.endsWith(".1") && it.name.contains(Cache.key(u)) }
  }
}

//读取Host
fun String?.getHost(): String {
  return if (this.isNullOrBlank()) "" else Uri.parse(this).host ?: this
}

//打开外部链接
fun String?.openOutLink() {
  if (!this.isNullOrBlank()) {
    try {
      val newUrl = if (this.startsWith("http", true)) this else "http://$this"
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
      ActivityUtils.getTopActivity()?.startActivity(intent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}