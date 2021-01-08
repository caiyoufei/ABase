package cc.abase.demo.component.sticky

import android.content.Context
import android.content.Intent
import androidx.lifecycle.rxLifeScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import cc.ab.base.ext.*
import cc.abase.demo.R
import cc.abase.demo.R.color
import cc.abase.demo.bean.local.*
import cc.abase.demo.component.comm.CommTitleActivity
import cc.abase.demo.item.*
import com.blankj.utilcode.util.StringUtils
import com.drakeet.multitype.MultiTypeAdapter
import kotlinx.android.synthetic.main.activity_sticky2.*
import kotlinx.coroutines.*

/**
 * Description:
 * @author: CASE
 * @date: 2020/4/21 14:34
 */
class StickyActivity2 : CommTitleActivity() {
  //<editor-fold defaultstate="collapsed" desc="外部跳转">
  companion object {
    fun startActivity(context: Context) {
      val intent = Intent(context, StickyActivity2::class.java)
      context.startActivity(intent)
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="变量">
  //模拟数据
  private val originDatas = mutableListOf<UserStickyBean>()

  //左边适配器
  private var leftAdapter = MultiTypeAdapter()

  //右边适配器
  private var rightAdapter = MultiTypeAdapter()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="XML">
  override fun layoutResContentId() = R.layout.activity_sticky2
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化View">
  override fun initContentView() {
    setTitleText(StringUtils.getString(R.string.title_sticky2))
    //同步两个列表
    initScrollListener()
    //加载更多
    sticky2RefreshLayout.setEnableRefresh(false)
    sticky2RefreshLayout.setEnableLoadMore(true)
    sticky2RefreshLayout.setOnLoadMoreListener {
      //停止惯性滚动
      sticky2Recycler1.stopInertiaRolling()
      sticky2Recycler2.stopInertiaRolling()
      rxLifeScope.launch {
        withContext(Dispatchers.IO) { delay(2000) }.let {
          fillData(originDatas.takeLast(40).toMutableList(), true)
          sticky2RefreshLayout?.finishLoadMore(true)
          sticky2RefreshLayout?.noMoreData()
        }
      }
    }
    //注册多类型
    leftAdapter.register(Sticky2LeftItem())
    leftAdapter.register(DividerItem())
    rightAdapter.register(Sticky2RightItem())
    rightAdapter.register(DividerItem())
    //设置适配器
    sticky2Recycler1.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
    sticky2Recycler2.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
    sticky2Recycler1.adapter = leftAdapter
    sticky2Recycler2.adapter = rightAdapter
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="两个列表同步滚动">
  //左边监听
  private lateinit var scrollListenerLeft: OnScrollListener

  //右边监听
  private lateinit var scrollListenerRight: OnScrollListener

  //初始化列表滑动监听
  private fun initScrollListener() {
    //两个竖向滑动列表位置同步
    scrollListenerLeft = object : OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) synRecyclerScroll(true)
      }

      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        sticky2Recycler2.removeOnScrollListener(scrollListenerRight)
        sticky2Recycler2.scrollBy(dx, dy)
        sticky2Recycler2.addOnScrollListener(scrollListenerRight)
      }
    }
    scrollListenerRight = object : OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) synRecyclerScroll(false)
      }

      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        sticky2Recycler1.removeOnScrollListener(scrollListenerLeft)
        sticky2Recycler1.scrollBy(dx, dy)
        sticky2Recycler1.addOnScrollListener(scrollListenerLeft)
      }
    }
    sticky2Recycler1.addOnScrollListener(scrollListenerLeft)
    sticky2Recycler2.addOnScrollListener(scrollListenerRight)
    //两个横向列表位置同步
    sticky2TopHSV.setScrollViewListener { _, x, y, _, _ -> sticky2BottomHSV.scrollTo(x, y) }
    sticky2BottomHSV.setScrollViewListener { _, x, y, _, _ -> sticky2TopHSV.scrollTo(x, y) }
  }

  //同步滚动距离
  private fun synRecyclerScroll(baseLeft: Boolean) {
    //没有控件不执行
    if (sticky2Recycler1.childCount == 0 || sticky2Recycler2.childCount == 0) return
    //参照方
    val layout1 = (if (baseLeft) sticky2Recycler1 else sticky2Recycler2).layoutManager as LinearLayoutManager
    //需要修改对齐的方
    val layout2 = (if (baseLeft) sticky2Recycler2 else sticky2Recycler1).layoutManager as LinearLayoutManager
    //找到位置
    val position = layout1.findFirstVisibleItemPosition()
    //偏移量
    var offset = 0
    //找到第一个View计算偏移量
    (if (baseLeft) sticky2Recycler1 else sticky2Recycler2).findViewHolderForAdapterPosition(position)?.let {
      offset = it.itemView.top
    }
    layout2.scrollToPositionWithOffset(position, offset)
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化Data">
  override fun initData() {
    showLoadingView()
    rxLifeScope.launch {
      withContext(Dispatchers.IO) {
        delay(2000)
        val temp = mutableListOf<UserStickyBean>()
        //随机增加个学生成绩
        for (i in 0..79) temp.add(UserStickyBean(score = UserScoreBean()))
        //按总成绩排序
        temp.sortedByDescending { it.score?.scores?.sum() }.toMutableList()
      }.let {
        originDatas.addAll(it)
        //第一页随机数量
        val size = (Math.random() * 40).toInt() + 1
        fillData(originDatas.take(size).toMutableList(), false)
        dismissLoadingView()
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="填充列表数据">
  private fun fillData(list: MutableList<UserStickyBean>, more: Boolean) {
    if (sticky2Recycler2 == null) return
    val items1 = mutableListOf<Any>()
    val items2 = mutableListOf<Any>()
    if (more) { //加载更多需要旧数据
      items1.addAll(leftAdapter.items)
      items2.addAll(rightAdapter.items)
    }
    //添加新数据
    list.forEach { userStickyBean ->
      userStickyBean.score?.let { score ->
        items1.add(userStickyBean.name)
        items1.add(DividerBean(heightPx = 1, bgColor = color.gray.xmlToColor()))
        items2.add(score)
        items2.add(DividerBean(heightPx = 1, bgColor = color.gray.xmlToColor()))
      }
    }
    leftAdapter.items = items1
    rightAdapter.items = items2
    leftAdapter.notifyDataSetChanged()
    rightAdapter.notifyDataSetChanged()
    //如果是第一次加载，判断是否可以加载更多
    if (!more) {
      sticky2Recycler1Parent?.visible()
      sticky2Recycler2?.visible()
      sticky2RefreshLayout?.hasMoreData()
    }
  }
  //</editor-fold>
}
