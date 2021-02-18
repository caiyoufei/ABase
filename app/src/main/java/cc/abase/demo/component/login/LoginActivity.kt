package cc.abase.demo.component.login

import android.content.Context
import android.content.Intent
import androidx.lifecycle.rxLifeScope
import cc.ab.base.ext.*
import cc.ab.base.utils.CcInputHelper
import cc.ab.base.utils.PressEffectHelper
import cc.abase.demo.R
import cc.abase.demo.component.comm.CommActivity
import cc.abase.demo.component.main.MainActivity
import cc.abase.demo.config.UserManager
import cc.abase.demo.constants.LengthConstants
import cc.abase.demo.constants.UiConstants
import cc.abase.demo.rxhttp.repository.UserRepository
import cc.abase.demo.utils.AppInfoUtils
import cc.abase.demo.utils.MMkvUtils
import cc.abase.demo.widget.biometric.*
import cc.abase.demo.widget.dialog.commAlertDialog
import com.blankj.utilcode.util.*
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Description:
 * @author: CASE
 * @date: 2019/10/10 14:58
 */
class LoginActivity : CommActivity() {
  //<editor-fold defaultstate="collapsed" desc="外部跳转">
  companion object {
    private const val KEY_INTENT_LOGIN_OUT = "KEY_INTENT_LOGIN_OUT"
    fun startActivity(context: Context) {
      val intent = Intent(context, LoginActivity::class.java)
      intent.putExtra(KEY_INTENT_LOGIN_OUT, ActivityUtils.getActivityList().any { ac -> ac is MainActivity })
      context.startActivity(intent)
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="XML">
  override fun layoutResId() = R.layout.activity_login
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="变量">
  //隐藏功能点击次数
  private var count = 0

  //指纹登录数据保存
  private var mmkv = MMKV.mmkvWithID("Biometric")
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化View">
  override fun initView() {
    loginAppInfo.setPadding(0, mStatusBarHeight, 0, 0)
    loginAppInfo.setOnClickListener {
      if ((++count) == 10) loginAppInfo.text = AppInfoUtils.getAppInfo()
    }
    checkSubmit()
    PressEffectHelper.alphaEffect(loginRegister)
    CcInputHelper.wrapCommCountLimit(loginEditAccount, LengthConstants.MAX_LEN_ACC, 0)
    CcInputHelper.wrapCommCountLimit(loginEditPassword, LengthConstants.MAX_LEN_PASS, 0)
    loginEditAccount.addTextWatcher { checkSubmit() }
    loginEditPassword.addTextWatcher { checkSubmit() }
    loginSubmit.click {
      showActionLoading()
      rxLifeScope.launch({
        val account = loginEditAccount.text.toString()
        val password = loginEditPassword.text.toString()
        withContext(Dispatchers.IO) { UserRepository.login(account, password) }.let { user ->
          "登录成功:${user.nickname}".logI()
          mmkv?.clearAll()
          saveBiometric(account, password)
        }
      }, { e ->
        e.toast()
      }, {}, {
        dismissActionLoading()
      })
    }
    loginRegister.click { RegisterActivity.startActivity(mContext) }
    //键盘弹窗直接处理登录操作View位置
    extKeyBoard { statusHeight, navigationHeight, keyBoardHeight ->
      if (keyBoardHeight > 0) {
        loginRegister.invisible()
        val array1 = intArrayOf(0, 0)
        val array2 = intArrayOf(0, 0)
        val array3 = intArrayOf(0, 0)
        loginRoot.getLocationOnScreen(array1)
        loginSubmit.getLocationOnScreen(array2)
        loginInputPassword.getLocationOnScreen(array3)
        array1[1] = array1[1] + loginRoot.height
        array2[1] = array2[1] + loginSubmit.height
        array3[1] = array3[1] + loginInputPassword.height
        loginSubmit.translationY = (keyBoardHeight - (array1[1] - array2[1])) * -1f - 10.dp2Px()
        if (array1[1] - array3[1] < loginSubmit.height + 10.dp2Px()) {
          loginInputPassword.translationY = (loginSubmit.height - (array1[1] - array3[1])) * -1f - 20.dp2Px()
          loginInputAccount.translationY = loginInputPassword.translationY
        }
      } else {
        loginRegister.visible()
        loginSubmit.translationY = 0f
        loginInputPassword.translationY = 0f
        loginInputAccount.translationY = 0f
      }
    }
    val isLoginOut = intent.getBooleanExtra(KEY_INTENT_LOGIN_OUT, false)
    if (isLoginOut || !checkBiometric()) {
      //读取上次的数据
      loginEditAccount.setText(MMkvUtils.getAccount())
      loginEditPassword.setText(MMkvUtils.getPassword())
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化Data">
  override fun initData() {
    //来到登录页默认需要清除数据
    UserManager.clearUserInfo()
    //关闭其他所有页面
    ActivityUtils.finishOtherActivities(javaClass)
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="账号密码登录">
  private fun checkSubmit() {
    val textAcc = loginEditAccount.text
    val textPass = loginEditPassword.text
    if (textAcc.isEmpty()) {
      loginInputAccount.hint = StringUtils.getString(R.string.login_account_hint)
    } else if (textAcc.isNotEmpty() && textAcc.length < LengthConstants.MIN_LEN_ACC) {
      loginInputAccount.hint = StringUtils.getString(R.string.login_account_short)
    } else {
      loginInputAccount.hint = ""
    }
    if (textPass.isEmpty()) {
      loginInputPassword.hint = StringUtils.getString(R.string.login_password_hint)
    } else if (textPass.isNotEmpty() && textPass.length < LengthConstants.MIN_LEN_PASS) {
      loginInputPassword.hint = StringUtils.getString(R.string.login_password_short)
    } else {
      loginInputPassword.hint = ""
    }
    val enable = textAcc.length >= LengthConstants.MIN_LEN_ACC && textPass.length >= LengthConstants.MIN_LEN_PASS
    loginSubmit.isEnabled = enable
    loginSubmit.alpha = if (enable) 1f else UiConstants.disable_alpha
    if (enable) {
      loginSubmit.pressEffectAlpha()
    } else {
      loginSubmit.pressEffectDisable()
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="检测指纹登录">
  //检测是否可以指纹登录
  private fun checkBiometric(): Boolean {
    val provider = BiometricProvider(this)
    provider.encrypt = false //默认为加密，这里改为解密
    val result = provider.checkBiometric()
    if (result == BiometricInterface.HW_AVAILABLE) {
      mmkv?.decodeBytes("IV")?.let { iv -> provider.ivBytes = iv } //解密需要IV
      mmkv?.decodeBytes("Biometric")?.let { b ->
        "读取的数据:${String(b)}".logE()
        commAlertDialog(supportFragmentManager, cancelable = false, outside = false) {
          title = "温馨提示"
          content = "是否使用指纹登录(切换账号请点取消)?"
          confirmText = "指纹登录"
          cancelText = "取消"
          confirmCallback = { authenticate(provider, b) }
        }
        return true
      }
    }
    return false
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="调用指纹登录">
  private var failCount = 0
  private fun authenticate(provider: BiometricProvider, bytes: ByteArray) {
    provider.tryAuthenticate({ cipher ->
      // 使用 cipher 对登录信息进行解密
      val loginInfo = cipher.doFinal(bytes)
      if (loginInfo != null) {
        val type = object : TypeToken<MutableMap<String, String>>() {}.type
        GsonUtils.fromJson<MutableMap<String, String>>(String(loginInfo), type)?.let { map ->
          map.toList().firstOrNull()?.let { pair ->
            showActionLoading()
            rxLifeScope.launch({
              val account = pair.first
              val password = pair.second
              withContext(Dispatchers.IO) { UserRepository.login(account, password) }.let { user ->
                "登录成功:${user.nickname}".logI()
                MainActivity.startActivity(mContext)
              }
            }, { e ->
              e.toast()
            }, {}, {
              dismissActionLoading()
            })
          }
        }
      }
    }, { code, msg ->
      failCount++
      if (code == BiometricInterface.ERROR_FAILED && failCount < 5) {
        "验证失败，请重试".toast()
        authenticate(provider, bytes)
      } else {
        "失败次数太多，暂停使用".toast()
        ActivityUtils.finishAllActivities()
      }
    })
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="登录成功处理指纹登录">
  //指纹识别通过后保存加密后的账号密码
  private fun saveBiometric(account: String, password: String) {
    val provider = BiometricProvider(this)
    val result = provider.checkBiometric()
    if (result == BiometricInterface.HW_AVAILABLE) {
      commAlertDialog(supportFragmentManager, cancelable = false, outside = false) {
        title = "温馨提示"
        content = "检测到您的手机支持指纹识别，是否开启指纹登录?"
        confirmText = "开启"
        cancelText = "忽略"
        confirmCallback = {
          provider.tryAuthenticate({ cipher ->
            // 使用 cipher 对登录信息进行加密并保存
            val map = hashMapOf(account to password)
            val saveMsg = GsonUtils.toJson(map)
            "加密前的数据:${saveMsg}".logE()
            cipher.doFinal(saveMsg.toByteArray())?.let { ap ->
              "保存的数据:${String(ap)}".logE()
              mmkv?.encode("Biometric", ap)
              mmkv?.encode("IV", cipher.iv)
            }
            MainActivity.startActivity(mContext)
          }, { code, msg ->
            msg.toast()
            MainActivity.startActivity(mContext)
          })
        }
      }
    } else {
      MainActivity.startActivity(mContext)
    }
  }
  //</editor-fold>
}