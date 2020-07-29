package com.readboy.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件操作工具类
 */
public class FileUtil {
	/**
	 * 压缩后转二进制
	 * 
	 * @param path 路径 ，inSampleSize 压缩比例
	 * @return
	 * @throws IOException
	 */
	public static byte[] read2ByteArray(String path,int inSampleSize) throws IOException {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 只获取图片的大小信息，而不是将整张图片载入在内存中，避免内存溢出
		BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 计算好压缩比例后，这次可以去加载原图了
		options.inSampleSize = inSampleSize; // 设置为刚才计算的压缩比例

		final Bitmap bm = BitmapFactory.decodeFile(path, options); // 解码文件
//bitmap使用base64转码
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 流转二进制数组
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static byte[] inputStream2ByteArray(InputStream in) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}
}
