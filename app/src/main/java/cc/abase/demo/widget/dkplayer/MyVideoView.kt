package cc.abase.demo.widget.dkplayer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import cc.ab.base.ext.loadImgHorizontal
import cc.ab.base.ext.loadNetVideoCover
import com.dueeeke.videocontroller.R
import com.dueeeke.videocontroller.StandardVideoController
import com.dueeeke.videocontroller.component.*
import com.dueeeke.videoplayer.player.VideoView
import com.dueeeke.videoplayer.util.PlayerUtils

/**
 * 将大部分控制封装后对外简单使用
 * Author:CASE
 * Date:2020/12/23
 * Time:13:16
 */
class MyVideoView : VideoView<MyExoMediaPlayer>, LifecycleObserver {
  //<editor-fold defaultstate="collapsed" desc="多构造">
  constructor(c: Context) : super(c, null, 0)
  constructor(c: Context, a: AttributeSet) : super(c, a, 0)
  constructor(c: Context, a: AttributeSet?, d: Int) : super(c, a, d)
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="变量">
  private val mCon = context

  //标准控制器
  private var controller: StandardVideoController = MyStandardController(mCon)

  //直播控制条
  private var liveCV: LiveControlView = LiveControlView(mCon)

  //点播控制条
  private var vodCV: VodControlView = MyVodControlView(mCon)

  //封面
  private var coverIv: ImageView

  //标题
  private var titleView: MyTitleView

  //返回按钮
  private var backIv: ImageView
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="初始化">
  init {
    //自感应生命周期
    (mCon as? AppCompatActivity)?.let { ac -> setLifecycleOwner(ac) }
    //根据屏幕方向自动进入/退出全屏
    controller.setEnableOrientation(false)
    //1.准备播放界面
    controller.addControlComponent(PrepareView(mCon).also { p ->
      coverIv = p.findViewById(R.id.thumb) //封面
      coverIv.scaleType = ImageView.ScaleType.FIT_CENTER
      p.findViewById<View>(R.id.start_play).setOnClickListener { //点击播放
        if (!mUrl.isNullOrBlank()) start()
      }
    })
    //2.自动完成播放界面
    controller.addControlComponent(CompleteView(mCon))
    //3.错误界面
    controller.addControlComponent(ErrorView(mCon))
    //4.标题
    controller.addControlComponent(MyTitleView(mCon).also { tv ->
      titleView = tv
      backIv = tv.findViewById(R.id.back)
      backIv.setOnClickListener { PlayerUtils.scanForActivity(context)?.onBackPressed() }
    })
    //5.滑动控制视图
    controller.addControlComponent(GestureView(mCon))
    //设置控制器到播放器
    setVideoController(controller)
    //默认不在列表中使用
    setInList(false)
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Lifecycle生命周期">
  private var mLifecycle: Lifecycle? = null

  //通过Lifecycle内部自动管理暂停和播放(如果不需要后台播放)
  private fun setLifecycleOwner(owner: LifecycleOwner?) {
    if (owner == null) {
      mLifecycle?.removeObserver(this)
      mLifecycle = null
    } else {
      mLifecycle?.removeObserver(this)
      mLifecycle = owner.lifecycle
      mLifecycle?.addObserver(this)
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  private fun onPauseVideo() {
    pause()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  private fun onResumeVideo() {
    resume()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  private fun onDestroyVideo() {
    release()
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="外部调用">
  //设置播放地址
  fun setPlayUrl(url: String, title: String? = null, cover: String? = null, autoPlay: Boolean = false, isLive: Boolean = false, ratio: Float = 16f / 9) {
    setUrl(url) //设置播放地址
    titleView.setTitle(if (title.isNullOrBlank()) url else title) //设置标题
    if (cover.isNullOrBlank()) coverIv.loadNetVideoCover(url, ratio)
    else coverIv.loadImgHorizontal(cover, ratio) //加载封面
    if (autoPlay) start() //开始播放
    //修改控制器
    controller.removeControlComponent(liveCV)
    controller.removeControlComponent(vodCV)
    controller.addControlComponent(if (isLive) liveCV else vodCV)
  }

  //设置是否用于列表中(默认不在列表中)
  fun setInList(inList: Boolean) {
    //竖屏也开启手势操作，默认关闭
    controller.setEnableInNormal(!inList)
    titleView.setInList(inList)
  }

  //设置返回按钮的显示状态
  fun setBackShow(visible: Int) {
    backIv.visibility = visible
    titleView.noFullBackVisibility = visible
  }

  //设置返回图标
  fun setBackResource(@DrawableRes id: Int) {
    backIv.setImageResource(id)
  }

  //外部获取控制器
  fun getMyController(): StandardVideoController {
    return controller
  }

  //外部回调视频尺寸
  var callSize: ((w: Int, h: Int) -> Unit)? = null

  //尺寸回调
  override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int) {
    super.onVideoSizeChanged(videoWidth, videoHeight)
    callSize?.invoke(videoWidth, videoHeight)
  }
  //</editor-fold>
}