package com.data;

import java.util.Timer;

public class AutoLauncher {
	private static Timer MumayiAppTimer;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MumayiAppTimer = new Timer();
		MumayiAppTimer.schedule(new MumayiApp(), 10*1000, 10*60 * 1000);
	}

}
