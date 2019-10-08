package cc.abase.demo.repository

import cc.abase.demo.repository.cache.GankCacheApi
import com.blankj.utilcode.util.PathUtils
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import io.reactivex.Single
import io.rx_cache2.DynamicKey
import io.rx_cache2.EvictProvider
import io.rx_cache2.internal.RxCache
import io.victoralbertos.jolyglot.GsonSpeaker
import java.io.File

/**
 * Description:不直接访问本类，通过对应的非缓存类进行访问
 * Repository-->Request-->CacheRepository
 * @author: caiyoufei
 * @date: 2019/10/8 15:48
 */
internal class CacheRepository private constructor() {
  //缓存目录
  private val cacheDir = PathUtils.getExternalAppDataPath() + File.separator + ".fuel"
  //缓存api
  private val cacheApi: GankCacheApi = RxCache.Builder()
      .persistence(
          if (File(cacheDir).exists()) {
            File(cacheDir)
          } else {
            File(cacheDir).mkdirs()
            File(cacheDir)
          }, GsonSpeaker()
      )
      .using<GankCacheApi>(GankCacheApi::class.java)

  private object SingletonHolder {
    val holder = CacheRepository()
  }

  companion object {
    val instance = SingletonHolder.holder
  }

  //带缓存的安卓数据获取
  internal fun androidList(
    single: Single<Result<String, FuelError>>,
    pageSize: DynamicKey,
    update: EvictProvider
  ): Single<Result<String, FuelError>> {
    return cacheApi.androidList(single, pageSize, update)
  }
}