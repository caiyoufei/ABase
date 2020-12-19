package cc.ab.base.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/**
 * Description:设备唯一id
 * https://www.cnblogs.com/qixingchao/p/11652408.html
 *
 * @author: CASE
 * @date: 2019/12/7 16:49
 */
public class DeviceIdUtil {
  private DeviceIdUtil() {
  }

  public static DeviceIdUtil getInstance() {
    return SingleTonHolder.INSTANCE;
  }

  /**
   * 获得设备硬件标识
   *
   * @param context 上下文
   * @return 设备硬件标识
   */
  @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
  public String getDeviceId(Context context) {
    StringBuilder sbDeviceId = new StringBuilder();

    //获得设备默认IMEI（>=6.0 需要ReadPhoneState权限）
    String imei = "";
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        == PackageManager.PERMISSION_GRANTED) {
      imei = getIMEI(context);
    }
    //获得AndroidId（无需权限）
    String androidid = getAndroidId(context);
    //获得设备序列号（无需权限）
    String serial = getSERIAL();
    //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
    String uuid = getDeviceUUID().replace("-", "");

    //追加imei
    if (imei != null && imei.length() > 0) {
      sbDeviceId.append(imei);
      sbDeviceId.append("|");
    }
    //追加androidid
    if (androidid != null && androidid.length() > 0) {
      sbDeviceId.append(androidid);
      sbDeviceId.append("|");
    }
    //追加serial
    if (serial != null && serial.length() > 0) {
      sbDeviceId.append(serial);
      sbDeviceId.append("|");
    }
    //追加硬件uuid
    if (uuid.length() > 0) {
      sbDeviceId.append(uuid);
    }

    //生成SHA1，统一DeviceId长度
    if (sbDeviceId.length() > 0) {
      try {
        byte[] hash = getHashByString(sbDeviceId.toString());
        String sha1 = bytesToHex(hash);
        if (sha1.length() > 0) {
          //返回最终的DeviceId
          return sha1;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    //如果以上硬件标识数据均无法获得，
    //则DeviceId默认使用系统随机数，这样保证DeviceId不为空
    return UUID.randomUUID().toString().replace("-", "");
  }

  //需要获得READ_PHONE_STATE权限，>=6.0，默认返回null
  @SuppressLint("HardwareIds")
  @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
  private String getIMEI(Context context) {
    try {
      TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (tm != null) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED) {
          return tm.getDeviceId();
        } else {
          return "";
        }
      } else {
        return "";
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * 获得设备的AndroidId
   *
   * @param context 上下文
   * @return 设备的AndroidId
   */
  @SuppressLint("HardwareIds")
  private String getAndroidId(Context context) {
    try {
      return Settings.Secure.getString(context.getContentResolver(),
          Settings.Secure.ANDROID_ID);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
   *
   * @return 设备序列号
   */
  @SuppressLint("HardwareIds")
  private String getSERIAL() {
    try {
      return Build.SERIAL;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * 获得设备硬件uuid
   * 使用硬件信息，计算出一个随机数
   *
   * @return 设备硬件uuid
   */
  @SuppressLint("HardwareIds")
  private String getDeviceUUID() {
    try {
      String dev = "3883756" +
          Build.BOARD.length() % 10 +
          Build.BRAND.length() % 10 +
          Build.DEVICE.length() % 10 +
          Build.HARDWARE.length() % 10 +
          Build.ID.length() % 10 +
          Build.MODEL.length() % 10 +
          Build.PRODUCT.length() % 10 +
          Build.SERIAL.length() % 10;
      return new UUID(dev.hashCode(),
          Build.SERIAL.hashCode()).toString();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    }
  }

  /**
   * 取SHA1
   *
   * @param data 数据
   * @return 对应的hash值
   */
  private byte[] getHashByString(String data) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
      messageDigest.reset();
      messageDigest.update(data.getBytes(StandardCharsets.UTF_8));
      return messageDigest.digest();
    } catch (Exception e) {
      return "".getBytes();
    }
  }

  /**
   * 转16进制字符串
   *
   * @param data 数据
   * @return 16进制字符串
   */
  private String bytesToHex(byte[] data) {
    StringBuilder sb = new StringBuilder();
    String stmp;
    for (byte mDatum : data) {
      stmp = (Integer.toHexString(mDatum & 0xFF));
      if (stmp.length() == 1) {
        sb.append("0");
      }
      sb.append(stmp);
    }
    return sb.toString().toUpperCase(Locale.CHINA);
  }

  private static class SingleTonHolder {
    private static final DeviceIdUtil INSTANCE = new DeviceIdUtil();
  }
}