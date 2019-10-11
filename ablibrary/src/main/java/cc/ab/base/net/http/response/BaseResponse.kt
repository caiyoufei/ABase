package cc.ab.base.net.http.response

/**
 * Description:网络请求返回的基类,接口文档没有不确定.
 * @author: caiyoufei
 * @date: 2019/9/22 18:54
 */
data class BaseResponse<out T>(
  val status: Int = -1,//异常接口使用的状态码
  val code: Int = -1,//正常接口使用的状态码
  val errorCode: Int = -1,//正常接口使用的状态码
  val message: String? = null,//code异常对应的信息提示
  val errorMsg: String? = null,//code异常对应的信息提示
  val throwType: String? = null,//code异常对应的服务端出错信息
  val data: T? = null,//正常返回的数据信息
  val fields: Map<String, Any>? = null//异常中返回的一些参数
)