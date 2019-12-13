package cc.abase.demo.component.playlist

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.viewpager.widget.ViewPager
import cc.ab.base.ext.*
import cc.abase.demo.R
import cc.abase.demo.component.comm.CommActivity
import cc.abase.demo.component.playlist.adapter.PlayPagerAdapter
import cc.abase.demo.component.playlist.adapter.PlayPagerAdapter.PagerHolder
import cc.abase.demo.component.playlist.view.PagerController
import cc.abase.demo.component.playlist.viewmoel.PlayPagerViewModel
import cc.abase.demo.repository.bean.local.VideoBean
import cc.abase.demo.widget.video.view.ExoVideoView
import com.dueeeke.videoplayer.player.VideoView
import kotlinx.android.synthetic.main.activity_play_pager.playPagerBack
import kotlinx.android.synthetic.main.activity_play_pager.playPagerViewPager

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/12/12 11:33
 */
class PlayPagerActivity : CommActivity() {

  companion object {
    fun startActivity(context: Context) {
      val intent = Intent(context, PlayPagerActivity::class.java)
      context.startActivity(intent)
    }
  }

  //当前播放位置
  private var mCurPos = 0
  //适配器
  private var mPlayPagerAdapter: PlayPagerAdapter? = null
  //不显示移动网络播放
  private var mController: PagerController? = null
  //播放控件
  private var mVideoView: ExoVideoView? = null
  //数据源
  private var mVideoList: MutableList<VideoBean> = mutableListOf()

  //数据层
  private val viewModel: PlayPagerViewModel by lazy {
    PlayPagerViewModel()
  }

  override fun layoutResId() = R.layout.activity_play_pager

  override fun initView() {
    //返回按钮
    playPagerBack.pressEffectAlpha()
    playPagerBack.click { onBackPressed() }
    //播放控件
    mVideoView = ExoVideoView(mContext)
    mVideoView?.setLooping(true)
    mVideoView?.setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT)
    //控制器
    mController = PagerController(mContext)
    mVideoView?.setVideoController(mController)
    //列表
    playPagerViewPager.offscreenPageLimit = 4
    playPagerViewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
      }

      override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
      ) {
      }

      override fun onPageSelected(position: Int) {
        if (position == mCurPos) return
        startPlay(position)
      }
    })
  }

  override fun initData() {
    viewModel.subscribe(this) {
      if (it.request.complete) {
        dismissLoadingView()
        mVideoList = it.videoList
        if (mPlayPagerAdapter == null) {
          initAdapter(it.videoList)
        } else {
          mPlayPagerAdapter?.setNewData(it.videoList)
        }
      }
    }
    showLoadingView()
    viewModel.loadData()
  }

  //初始化adapter
  private fun initAdapter(datas: MutableList<VideoBean>) {
    if (mPlayPagerAdapter == null && playPagerViewPager != null) {
      mPlayPagerAdapter = PlayPagerAdapter(datas)
      playPagerViewPager.adapter = mPlayPagerAdapter
      //随机从某一个开始播放
      val index = (Math.random() * datas.size).toInt()
      if (index == 0) {
        startPlay(0)
      } else {
        playPagerViewPager.currentItem = index
      }
    }
  }

  //开始播放
  private fun startPlay(position: Int) {
    val count: Int = playPagerViewPager.childCount
    for (i in 0 until count) {
      val itemView: View = playPagerViewPager.getChildAt(i)
      val viewHolder: PagerHolder = itemView.tag as PagerHolder
      if (viewHolder.mPosition == position) {
        mVideoView?.release()
        mVideoView?.removeParent()
        val videoBean: VideoBean = mVideoList[viewHolder.mPosition]
        mVideoView?.setUrl(videoBean.url)
        mController?.addControlComponent(viewHolder.mPagerItemView, true)
        viewHolder.mPlayerContainer?.addView(mVideoView, 0)
        mVideoView?.start()
        mCurPos = position
        break
      } else if (position > 0 && viewHolder.mPosition == position - 1) {//预加载上一个数据，否则滑动可能出现复用的数据
        mPlayPagerAdapter?.fillData(mVideoList[viewHolder.mPosition], viewHolder)

      } else if (position < mVideoList.size - 1 && viewHolder.mPosition == position + 1) {//预加载下一个数据，否则滑动可能出现复用的数据
        mPlayPagerAdapter?.fillData(mVideoList[viewHolder.mPosition], viewHolder)
        break
      }
    }
  }

  override fun onResume() {
    super.onResume()
    mVideoView?.resume()
  }

  override fun onPause() {
    super.onPause()
    mVideoView?.pause()
  }

  override fun onDestroy() {
    super.onDestroy()
    mVideoView?.release()
  }

  override fun onBackPressed() {
    if (mVideoView == null || mVideoView?.onBackPressed() == false) {
      super.onBackPressed()
    }
  }
}