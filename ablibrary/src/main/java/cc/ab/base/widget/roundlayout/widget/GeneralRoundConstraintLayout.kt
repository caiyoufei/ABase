package cc.ab.base.widget.roundlayout.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cc.ab.base.R
import cc.ab.base.widget.roundlayout.abs.GeneralRoundViewImpl
import cc.ab.base.widget.roundlayout.abs.IRoundView

/**
 * GeneralRoundConstraintLayout
 * @author minminaya
 * @email minminaya@gmail.com
 * @time Created by 2019/6/8 0:36
 *
 */
open class GeneralRoundConstraintLayout : ConstraintLayout, IRoundView {
    private lateinit var generalRoundViewImpl: GeneralRoundViewImpl

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(this, context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(this, context, attrs)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        generalRoundViewImpl.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        generalRoundViewImpl.beforeDispatchDraw(canvas)
        super.dispatchDraw(canvas)
        generalRoundViewImpl.afterDispatchDraw(canvas)
    }

    private fun init(view: View, context: Context, attributeSet: AttributeSet?) {
        generalRoundViewImpl = GeneralRoundViewImpl(
            view,
            context,
            attributeSet,
            R.styleable.GeneralRoundConstraintLayout,
            R.styleable.GeneralRoundRelativeLayout_corner_radius
        )
    }

    override fun setCornerRadius(cornerRadius: Float) {
        generalRoundViewImpl.setCornerRadius(cornerRadius)
    }

}