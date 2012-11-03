/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lab;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author LostMekka
 */
public class ShutdownManager {
	
	private class ShutdownThread extends Thread {
		private ShutdownManager mgr;
		private boolean release = false;
		public ShutdownThread(ShutdownManager mgr) {
			this.mgr = mgr;
		}
		public void releaseSystem(){
			release = true;
		}
		@Override
		public void run() {
			System.out.println("shutdown requested. finishing whatever needs to be finished...");
			mgr.onShutdown();
			while(!release) {
				try {
					sleep(500);
				} catch (InterruptedException ex) {}
			}
		}
	}

	private ShutdownThread shutdownThread = null;
	private boolean shutdownRequested = false;
	
	public void onShutdown() {
		shutdownRequested = true;
	}
	
	protected void beginShutdownInjection(){
		if(shutdownThread == null){
			shutdownThread = new ShutdownThread(this);
			Runtime.getRuntime().addShutdownHook(shutdownThread);
		}
	}
		
	protected void endShutdownInjection(){
		if(shutdownThread != null){
			if(shutdownRequested){
				shutdownThread.releaseSystem();
			} else {
				Runtime.getRuntime().removeShutdownHook(shutdownThread);
			}
		}
	}

	public boolean isShutdownRequested() {
		return shutdownRequested;
	}
	
}
