package cc.abase.demo.component.rxhttp.parser

import cc.ab.base.net.http.response.BaseResponse
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException

/**
 * https://github.com/liujingxing/okhttp-RxHttp/wiki/%E9%AB%98%E7%BA%A7%E5%8A%9F%E8%83%BD#%E8%87%AA%E5%AE%9A%E4%B9%89Parser
 * Response<T> 数据解析器,解析完成对Response对象做判断,如果ok,返回数据 T
 */
@Parser(name = "ResponseWan")
class ResponseWanParser<T> : AbstractParser<T> {

  protected constructor() : super()

  constructor(type: Class<T>) : super(type)

  @Throws(IOException::class)
  override fun onParse(response: okhttp3.Response): T {
    val type = ParameterizedTypeImpl.get(BaseResponse::class.java, mType) //获取泛型类型
    val data = convert<BaseResponse<T>>(response, type)
    var t: T? = data.data //获取data字段
    if (t == null && mType === String::class.java) {
      /*
       * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
       * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
       * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
       */
      t = data.errorMsg as T
    }
    if (data.errorCode != 0 || t == null) {//code不等于0，说明数据不正确，抛出异常
      throw ParseException(data.errorCode.toString(), data.errorMsg, response)
    }
    return t
  }
}