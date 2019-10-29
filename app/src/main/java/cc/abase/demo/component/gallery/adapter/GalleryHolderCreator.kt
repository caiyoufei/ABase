package cc.abase.demo.component.gallery.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import cc.ab.base.ext.load
import cc.ab.base.widget.discretescrollview.holder.DiscreteHolder
import cc.ab.base.widget.discretescrollview.holder.DiscreteHolderCreator
import cc.abase.demo.R
import kotlinx.android.synthetic.main.layout_gallery.view.itemGallery
import me.panpf.sketch.SketchImageView
import me.panpf.sketch.decode.ImageAttrs
import me.panpf.sketch.request.*

/**
 * Description:
 * @author: caiyoufei
 * @date: 2019/10/29 17:25
 */
class GalleryHolderCreator : DiscreteHolderCreator {
  override fun createHolder(itemView: View): DiscreteHolder<String> = GalleryHolder(itemView)

  override fun getLayoutId() = R.layout.layout_gallery
}

class GalleryHolder(view: View) : DiscreteHolder<String>(view) {
  //图片
  private var imageView: SketchImageView? = null
  //防止每次都要获取填充方式
  private var hashMap: HashMap<String, ImageView.ScaleType?> = HashMap()

  override fun initView(itemView: View) {
    imageView = itemView.itemGallery
    imageView?.isZoomEnabled = true
  }

  override fun updateUI(
    data: String?,
    position: Int,
    count: Int
  ) {
    //统一宽度适配屏幕
    if (data != null) {
      val scaleType: ImageView.ScaleType? = hashMap[data]
      if (scaleType == null) {
        imageView?.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView?.displayListener = object : DisplayListener {
          override fun onStarted() {
          }

          override fun onCanceled(cause: CancelCause) {
          }

          override fun onError(cause: ErrorCause) {
          }

          override fun onCompleted(
            drawable: Drawable,
            imageFrom: ImageFrom,
            imageAttrs: ImageAttrs
          ) {
            val scanType = if (drawable.intrinsicWidth > drawable.intrinsicHeight) {
              ImageView.ScaleType.FIT_CENTER
            } else {
              ImageView.ScaleType.CENTER_CROP
            }
            imageView?.scaleType = scanType
            hashMap[data] = scanType
          }
        }
      } else {
        imageView?.scaleType = scaleType
      }
    }
    this.imageView?.load(data)
  }
}