package com.zhongsjie.access;


import com.zhongsjie.domain.SpikeUser;

public class UserContext {
	/**
	 * ThreadLocal多线程下线程安全的方式，和当前线程绑定
	 */
	private static ThreadLocal<SpikeUser> userHolder = new ThreadLocal<>();
	
	public static void setUser(SpikeUser user) {
		userHolder.set(user);
	}
	
	public static SpikeUser getUser() {
		return userHolder.get();
	}

}
