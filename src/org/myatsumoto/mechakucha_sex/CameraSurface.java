package org.myatsumoto.mechakucha_sex;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements
		SurfaceHolder.Callback {

	private Camera camera;

	private SurfaceHolder surfaceHolder;

	public CameraSurface(Context context) {
		super(context);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		camera.stopPreview();
		if (isPortrait()) {
			camera.setDisplayOrientation(90);
		} else {
			camera.setDisplayOrientation(0);
		}
		camera.startPreview();
	}

	private boolean isPortrait() {
		return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.release();
		camera = null;
	}

}
