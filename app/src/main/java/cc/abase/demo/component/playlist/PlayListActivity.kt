package cc.abase.demo.component.playlist

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.ab.base.ext.mContext
import cc.ab.base.ext.removeParent
import cc.ab.base.ui.viewmodel.DataState
import cc.abase.demo.R
import cc.abase.demo.bean.local.NoMoreBean
import cc.abase.demo.bean.local.VideoBean
import cc.abase.demo.component.comm.CommTitleActivity
import cc.abase.demo.component.playlist.viewmoel.PlayListViewModel
import cc.abase.demo.item.NoMoreItem
import cc.abase.demo.item.VideoListItem
import cc.abase.demo.widget.dkplayer.MyVideoView
import com.blankj.utilcode.util.StringUtils
import com.drakeet.multitype.MultiTypeAdapter
import kotlinx.android.synthetic.main.activity_play_list.playListRecycler
import kotlinx.android.synthetic.main.item_list_video.view.itemVideoContainer

/**
 * Description:https://github.com/dueeeke/DKVideoPlayer
 * @author: CASE
 * @date: 2019/12/12 11:32
 */
class PlayListActivity : CommTitleActivity() {
  //<editor-fold defaultstate="collapsed" desc="外部跳转">
  companion object {
    fun startActivity(context: Context) {
      val intent = Intent(context, PlayListActivity::class.java)
      context.startActivity(intent)
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="变量">
  //播放器
  private var mVideoView: MyVideoView? = null

  //当前播放的位置
  private var mCurPos = -1

  //数据层
  private val viewModel: PlayListViewModel by lazy { PlayListViewModel() }

  //适配器
  private val multiTypeAdapter = MultiTypeAdapter()
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="XML">
  override fun layoutResContentId() = R.layout.activity_play_list
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化View">
  override fun initContentView() {
    setTitleText(StringUtils.getString(R.string.title_play_list))
    multiTypeAdapter.register(VideoListItem { startPlay(multiTypeAdapter.items.indexOf(it)) })
    multiTypeAdapter.register(NoMoreItem())
    //播放相关
    mVideoView = MyVideoView(mContext)
    mVideoView?.setInList(true)
    //列表相关
    playListRecycler.layoutManager = LinearLayoutManager(mContext)
    playListRecycler.adapter = multiTypeAdapter
    playListRecycler.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
      override fun onChildViewDetachedFromWindow(view: View) { //非全屏滑出去释放掉
        view.itemVideoContainer?.getChildAt(0)?.let { if (it == mVideoView && mVideoView?.isFullScreen == false) releaseVideoView() }
      }

      override fun onChildViewAttachedToWindow(view: View) {
      }
    })
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化Data">
  override fun initData() {
    viewModel.videoLiveData.observe(this) {
      when (it) {
        is DataState.Start -> showLoadingView()
        is DataState.Complete -> dismissLoadingView()
        is DataState.SuccessRefresh -> {
          val items = mutableListOf<Any>()
          it.data?.let { list ->
            items.addAll(list)
            items.add(NoMoreBean())
          }
          multiTypeAdapter.items = items
          multiTypeAdapter.notifyDataSetChanged()
        }
        else -> {
        }
      }
    }
    viewModel.loadData()
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="开始播放">
  //开始播放
  private fun startPlay(position: Int) {
    if (mCurPos == position) return
    if (mCurPos != -1) releaseVideoView()
    playListRecycler.layoutManager?.findViewByPosition(position)
        ?.let {
          mVideoView?.removeParent()
          it.itemVideoContainer.addView(mVideoView)
          val videoBean = multiTypeAdapter.items[position] as? VideoBean
          mVideoView?.setPlayUrl(url = videoBean?.url ?: "", autoPlay = true, needHolder = false)
          mCurPos = position
        }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="释放播放">
  //释放播放
  private fun releaseVideoView() {
    mVideoView?.let {
      it.release()
      it.removeParent()
      mCurPos = -1
    }
  }
  //</editor-fold>
}