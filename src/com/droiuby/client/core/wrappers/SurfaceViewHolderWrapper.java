package com.droiuby.client.core.wrappers;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

public class SurfaceViewHolderWrapper implements SurfaceHolder {

	SurfaceHolder holder;

	public SurfaceViewHolderWrapper(SurfaceHolder holder) {
		this.holder = holder;
	}

	public void addCallback(Callback callback) {
		holder.addCallback(callback);
	}

	public Surface getSurface() {
		return holder.getSurface();
	}

	public Rect getSurfaceFrame() {
		return holder.getSurfaceFrame();
	}

	public boolean isCreating() {
		return holder.isCreating();
	}

	public Canvas lockCanvas() {
		return holder.lockCanvas();
	}

	public Canvas lockCanvas(Rect dirty) {
		return holder.lockCanvas(dirty);
	}

	public void removeCallback(Callback callback) {
		holder.removeCallback(callback);
	}

	public void setFixedSize(int width, int height) {
		holder.setFixedSize(width, height);
	}

	public void setFormat(int format) {
		holder.setFormat(format);
	}

	public void setKeepScreenOn(boolean screenOn) {
		holder.setKeepScreenOn(screenOn);
	}

	public void setSizeFromLayout() {
		holder.setSizeFromLayout();
	}

	@Deprecated
	public void setType(int type) {
		holder.setType(type);
	}

	public void unlockCanvasAndPost(Canvas canvas) {
		holder.unlockCanvasAndPost(canvas);
	}
}
