package cc.abase.demo.repository

import cc.ab.base.net.http.response.ApiException
import cc.ab.base.net.http.response.BaseResponse
import cc.abase.demo.R.string
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.Utils
import io.reactivex.Single
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import java.io.File

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/10/9 21:47
 */
abstract class BaseCacheRepository<T>(classProviders: Class<T>) {
  //缓存目录
  private val cacheDir = PathUtils.getExternalAppDataPath() + File.separator + ".fuel"
  //缓存api
  var cacheApi: T = RxCache.Builder()
      .persistence(
          if (File(cacheDir).exists()) {
            File(cacheDir)
          } else {
            File(cacheDir).mkdirs()
            File(cacheDir)
          }, GsonSpeaker()
      )
      .using<T>(classProviders)

  //统一处理base的数据
  fun <T> justRespons(response: BaseResponse<T>): Single<T> {
    return if (response.errorCode == 0 && response.data != null) {
      Single.just(response.data)
    } else {
      Single.error(
          if (response.data == null) {
            ApiException(
                msg = Utils.getApp().getString(
                    string.service_no_data
                )
            )
          } else {
            ApiException(code = response.errorCode, msg = response.errorMsg)
          }
      )
    }
  }
}