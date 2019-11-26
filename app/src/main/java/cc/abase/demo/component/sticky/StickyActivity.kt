package cc.abase.demo.component.sticky

import android.content.Context
import android.content.Intent
import cc.abase.demo.R
import cc.abase.demo.component.comm.CommTitleActivity
import cc.abase.demo.component.sticky.adapter.StickyHeaderAdapter
import cc.abase.demo.component.sticky.widget.StickyHeaderLinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import kotlinx.android.synthetic.main.activity_sticky.stickyRecycler

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/11/26 16:54
 */
class StickyActivity : CommTitleActivity() {

  companion object {
    fun startActivity(context: Context) {
      val intent = Intent(context, StickyActivity::class.java)
      context.startActivity(intent)
    }
  }

  override fun layoutResContentId() = R.layout.activity_sticky

  override fun initContentView() {
    setTitleText(StringUtils.getString(R.string.title_sticky))
    stickyRecycler.layoutManager = StickyHeaderLinearLayoutManager<StickyHeaderAdapter>(this)
    stickyRecycler.adapter =
      StickyHeaderAdapter(this)
  }

  override fun initData() {
  }
}