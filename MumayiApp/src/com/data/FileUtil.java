package com.data;

import java.io.File;

public class FileUtil {
	/**
	 * 
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return
	 */

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 
	 * 删除目录下的所有文件
	 */

	public static boolean deleteDir(String dir) {
		boolean result = false;
		File file = new File(dir);
		if (file.exists()) {
			result = deleteDir(file);
		} else {
			System.out.println("缓存目录不存在");
		}
		return result;

	}

	/**
	 * 
	 * 删除文件
	 * 
	 * @param path
	 */

	public static boolean deleteFile(String path) {
		boolean bl;
		File file = new File(path);
		if (file.exists()) {
			bl = file.delete();
		} else {
			bl = false;
		}
		return bl;
	}
}
