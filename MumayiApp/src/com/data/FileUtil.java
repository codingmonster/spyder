package com.data;

import java.io.File;

public class FileUtil {
	/**
	 * 
	 * �ݹ�ɾ��Ŀ¼�µ������ļ�����Ŀ¼�������ļ�
	 * 
	 * @param dir
	 *            ��Ҫɾ�����ļ�Ŀ¼
	 * @return
	 */

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// �ݹ�ɾ��Ŀ¼�е���Ŀ¼��
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// Ŀ¼��ʱΪ�գ�����ɾ��
		return dir.delete();
	}

	/**
	 * 
	 * ɾ��Ŀ¼�µ������ļ�
	 */

	public static boolean deleteDir(String dir) {
		boolean result = false;
		File file = new File(dir);
		if (file.exists()) {
			result = deleteDir(file);
		} else {
			System.out.println("����Ŀ¼������");
		}
		return result;

	}

	/**
	 * 
	 * ɾ���ļ�
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
