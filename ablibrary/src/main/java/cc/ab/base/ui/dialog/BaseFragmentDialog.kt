package cc.ab.base.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import cc.ab.base.ext.*

/**
 *description: Dialog的基类.
 *@date 2019/5/6 16:06.
 *@author: YangYang.
 */
abstract class BaseFragmentDialog : DialogFragment() {

  var mWidth = WRAP_CONTENT
  var mHeight = WRAP_CONTENT
  var mGravity = Gravity.CENTER
  var mOffsetX = 0
  var mOffsetY = 0
  var mAnimation: Int? = null
  var touchOutside: Boolean = true
  var mSoftInputMode: Int = SOFT_INPUT_STATE_ALWAYS_HIDDEN
  var lowerBackground = false // 是否降级背景，例如图片预览的时候不可以降级（设置Activity的透明度）

  /****** listener ******/
  private var viewLoadedListener: ((View) -> Unit)? = null
  private var showListener: (() -> Unit)? = null
  private var disListener: (() -> Unit)? = null

  private var contentView: View? = null
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    setStyle()
    if (contentView == null) {
      val view = inflater.inflate(contentLayout(), container, false)
      contentView = view
      viewLoadedListener?.invoke(view)
    } else {
      contentView?.parent?.let { parent ->
        ((parent as ViewGroup).removeView(contentView))
      }
    }
    return contentView
  }

  /**** 降低背景的Window等级 ****/

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    val c = context
    if (lowerBackground && c != null) setBackgroundAlpha(c, 0.3F)
    super.onViewCreated(view, savedInstanceState)
    initView(view, savedInstanceState)
  }


  override fun onDestroyView() {
    val c = context
    if (lowerBackground && c != null) setBackgroundAlpha(c, 1F)
    super.onDestroyView()
  }

  // 黑暗 0.0F ~ 1.0F 透明
  protected open fun setBackgroundAlpha(
    context: Context,
    alpha: Float
  ) {
    val act = context as? Activity ?: return
    val attributes = act.window.attributes
    attributes.alpha = alpha
    act.window.attributes = attributes
  }

  /**
   * 防止同时弹出两个一样类型的dialog，如果需要弹2个类型一样的dialog但内容不同，请设置不同的tag
   */
  override fun show(
    manager: FragmentManager,
    tag: String?
  ) {
    val ac = manager.getContext() ?: context
    if (ac == null || (ac as Activity).isFinishing) {
      return
    }
    //保存的key以 “页面名称 + 弹窗名字 + tag” 作为标识符
    val keyStr = String.format("%s_%s_%s", ac.javaClass.simpleName, this.javaClass.simpleName, tag ?: "")
    //防止相同页面500ms内重复弹出相同dialog
    ac.mDialogTimes.let { list ->
      var has = false
      //找到上次弹窗时间并更新
      list.firstOrNull { it.first == keyStr }?.let { p ->
        has = true
        if (System.currentTimeMillis() - p.second < 500) {
          return
        } else list[list.indexOf(p)] = Pair(keyStr, System.currentTimeMillis())
      }
      //第一次弹窗，添加弹窗时间
      if (!has) list.add(Pair(keyStr, System.currentTimeMillis()))
    }
    showListener?.invoke()
//        super.show(manager, tag)
    setBooleanField("mDismissed", false)
    setBooleanField("mShownByMe", true)
    val ft = manager.beginTransaction()
    ft.add(this, tag)
    ft.commitAllowingStateLoss()
  }

  private fun setBooleanField(
    fieldName: String,
    value: Boolean
  ) {
    try {
      val field = DialogFragment::class.java.getDeclaredField(fieldName)
      field.isAccessible = true
      field.set(this, value)
    } catch (e: NoSuchFieldException) {
      e.printStackTrace()
    } catch (e: IllegalAccessException) {
      e.printStackTrace()
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    disListener?.invoke()

    super.onDismiss(dialog)
  }

  fun onShow(listener: () -> Unit) {
    showListener = listener
  }

  fun onDismiss(listener: () -> Unit) {
    disListener = listener
  }

  /**
   * 布局加载完成监听事件
   * 用于 获取布局中的 view
   */
  fun onViewLoaded(listener: (View) -> Unit) {
    viewLoadedListener = listener
  }

  /**
   * 设置统一样式
   */
  private fun setStyle() {
    //获取Window
    val window = dialog?.window
    //无标题
    dialog?.requestWindowFeature(DialogFragment.STYLE_NO_TITLE)
    // 透明背景
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    if (lowerBackground) window?.setDimAmount(0F) // 去除 dialog 弹出的阴影
    dialog?.setCanceledOnTouchOutside(touchOutside)
    //设置宽高
    window?.decorView?.setPadding(0, 0, 0, 0)
    val wlp = window?.attributes
    wlp?.width = mWidth
    wlp?.height = mHeight
    //设置对齐方式
    wlp?.gravity = mGravity
    //设置偏移量
    wlp?.x = dialog?.context?.dp2px(mOffsetX) ?: 0
    wlp?.y = dialog?.context?.dp2px(mOffsetY) ?: 0
    wlp?.softInputMode = mSoftInputMode
    //设置动画
    mAnimation?.also { window?.setWindowAnimations(it) }
    window?.attributes = wlp
  }

  //-----------------------需要重写-----------------------//
  //XML布局
  protected abstract fun contentLayout(): Int

  //初始化
  protected abstract fun initView(view: View, savedInstanceState: Bundle?)

}