package cc.abase.demo.component.login

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.lifecycle.rxLifeScope
import cc.ab.base.ext.*
import cc.ab.base.utils.CcInputHelper
import cc.abase.demo.R
import cc.abase.demo.component.comm.CommBindTitleActivity
import cc.abase.demo.component.main.MainActivity
import cc.abase.demo.constants.LengthConstants
import cc.abase.demo.constants.UiConstants
import cc.abase.demo.databinding.ActivityRegisterBinding
import cc.abase.demo.rxhttp.repository.UserRepository
import com.blankj.utilcode.util.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Description:
 * @author: CASE
 * @date: 2019/10/10 21:03
 */
class RegisterActivity : CommBindTitleActivity<ActivityRegisterBinding>() {
  //<editor-fold defaultstate="collapsed" desc="外部跳转">
  companion object {
    fun startActivity(context: Context) {
      val intent = Intent(context, RegisterActivity::class.java)
      context.startActivity(intent)
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化View">
  override fun initContentView() {
    setTitleText(StringUtils.getString(R.string.login_register_hint))
    checkSubmit()
    CcInputHelper.wrapCommCountLimit(viewBinding.registerEditAccount, LengthConstants.MAX_LEN_ACC, 0)
    CcInputHelper.wrapCommCountLimit(viewBinding.registerEditPassword1, LengthConstants.MAX_LEN_PASS, 0)
    CcInputHelper.wrapCommCountLimit(viewBinding.registerEditPassword2, LengthConstants.MAX_LEN_PASS, 0)
    viewBinding.registerEditAccount.addTextWatcher { checkSubmit() }
    viewBinding.registerEditPassword1.addTextWatcher { checkSubmit() }
    viewBinding.registerEditPassword2.addTextWatcher { checkSubmit() }
    viewBinding.registerSubmit.click {
      showActionLoading()
      rxLifeScope.launch({
        withContext(Dispatchers.IO) {
          UserRepository.register(
              viewBinding.registerEditAccount.text.toString(),
              viewBinding.registerEditPassword1.text.toString(),
              viewBinding.registerEditPassword2.text.toString()
          )
        }.let {
          MainActivity.startActivity(mContext)
        }
      }, { e ->
        e.toast()
      }, {}, {
        dismissActionLoading()
      })
    }
    extKeyBoard { statusHeight, navigationHeight, keyBoardHeight ->
      if (keyBoardHeight > 0) {
        val array1 = intArrayOf(0, 0)
        val array2 = intArrayOf(0, 0)
        val array3 = intArrayOf(0, 0)
        viewBinding.registerRoot.getLocationOnScreen(array1)
        viewBinding.registerSubmit.getLocationOnScreen(array2)
        viewBinding.registerInputPassword2.getLocationOnScreen(array3)
        array1[1] = array1[1] + viewBinding.registerRoot.height
        array2[1] = array2[1] + viewBinding.registerSubmit.height
        array3[1] = array3[1] + viewBinding.registerInputPassword2.height
        viewBinding.registerSubmit.translationY = (keyBoardHeight - (array1[1] - array2[1])) * -1f - 10.dp2px()
        if (array1[1] - array3[1] < viewBinding.registerSubmit.height + 10.dp2px()) {
          viewBinding.registerInputPassword2.translationY = (viewBinding.registerSubmit.height - (array1[1] - array3[1])) * -1f - 20.dp2px()
          viewBinding.registerInputPassword1.translationY = viewBinding.registerInputPassword2.translationY
          viewBinding.registerInputAccount.translationY = viewBinding.registerInputPassword2.translationY
        }
      } else {
        viewBinding.registerSubmit.translationY = 0f
        viewBinding.registerInputAccount.translationY = 0f
        viewBinding.registerInputPassword1.translationY = 0f
        viewBinding.registerInputPassword2.translationY = 0f
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="注册操作">
  private fun checkSubmit() {
    val textAcc = viewBinding.registerEditAccount.text
    val textPass1 = viewBinding.registerEditPassword1.text
    val textPass2 = viewBinding.registerEditPassword2.text
    if (textAcc.isEmpty()) {
      viewBinding.registerInputAccount.hint = StringUtils.getString(R.string.login_account_hint)
    } else if (textAcc.isNotEmpty() && textAcc.length < LengthConstants.MIN_LEN_ACC) {
      viewBinding.registerInputAccount.hint = StringUtils.getString(R.string.login_account_short)
    } else {
      viewBinding.registerInputAccount.hint = ""
    }
    if (textPass1.isEmpty()) {
      viewBinding.registerInputPassword1.hint = StringUtils.getString(R.string.login_password_hint)
    } else if (textPass1.isNotEmpty() && textPass1.length < LengthConstants.MIN_LEN_PASS) {
      viewBinding.registerInputPassword1.hint = StringUtils.getString(R.string.login_password_short)
    } else {
      viewBinding.registerInputPassword1.hint = ""
    }
    if (textPass2.isEmpty()) {
      viewBinding.registerInputPassword2.hint = StringUtils.getString(R.string.login_password_again_hint)
    } else if (!TextUtils.equals(textPass1, textPass2)) {
      viewBinding.registerInputPassword2.hint = StringUtils.getString(R.string.login_password_not_same)
    } else {
      viewBinding.registerInputPassword2.hint = ""
    }
    val enable = textAcc.length >= LengthConstants.MIN_LEN_ACC &&
        textPass1.length >= LengthConstants.MIN_LEN_PASS &&
        TextUtils.equals(viewBinding.registerEditPassword1.text, viewBinding.registerEditPassword2.text)
    viewBinding.registerSubmit.isEnabled = enable
    viewBinding.registerSubmit.alpha = if (enable) 1f else UiConstants.disable_alpha
    if (enable) {
      viewBinding.registerSubmit.pressEffectAlpha()
    } else {
      viewBinding.registerSubmit.pressEffectDisable()
    }
  }
  //</editor-fold>
}