package org.myatsumoto.mechakucha_sex;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BalloonSurface extends SurfaceView implements
		SurfaceHolder.Callback, Runnable {

	private static final String TAG = BalloonSurface.class.getSimpleName();

	private static final long INTERVAL_PERIOD = 33; // 30fps

	private ScheduledExecutorService scheduledExecutorService;

	private SurfaceHolder surfaceHolder;

	private BitmapBalloonView konoatoBalloon;

	private Bitmap selectedBitmap;

	private boolean isDragging = false;

	private boolean isConfigurationChanged = false;

	private Size currentCanvasSize;

	private Size extendedBitmapSize;

	private Configuration currentConfiguration;

	private float touchedBalloonX, touchedBalloonY;

	private CanvasPosition portraitCanvasPosition, landscapeCanvasPosition;

	public BalloonSurface(Context context) {
		super(context);
		surfaceHolder = getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		surfaceHolder.addCallback(this);
		setZOrderOnTop(true);
	}

	@Override
	public void run() {
		Canvas canvas = surfaceHolder.lockCanvas();
		renderToCanvas(canvas);
		surfaceHolder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
		Log.d(TAG, "surface changed");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (konoatoBalloon.isTouched(event.getX(), event.getY())) {
				touchedBalloonX = event.getX() - konoatoBalloon.getLeft();
				touchedBalloonY = event.getY() - konoatoBalloon.getTop();
				isDragging = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDragging) {
				konoatoBalloon.move(event.getX() - touchedBalloonX,
						event.getY() - touchedBalloonY);
			}
			break;
		case MotionEvent.ACTION_UP:
			isDragging = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			isDragging = false;
			break;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surface created");
		initBallonView();
		startSurfaceRendering();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surface destroyed");
		stopSurfaceRendering();
	}

	public void setSelectedBitmap(Bitmap selectedBitmap) {
		this.selectedBitmap = selectedBitmap;
	}

	public void onConfigurationChanged(Configuration config) {
		Log.d(TAG, "configuration changed");
		currentConfiguration = config;
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
			saveToLandscapeBalloonPosition();
		} else {
			saveToPortraitBalloonPosition();
		}
		isConfigurationChanged = true;
	}

	public Bitmap requestCompositeBitmap() {
		if (isKonoatoBallooonInclueded()) {
			return createCompositeBitmpWithoutBlank();
		} else {
			return createCompositeBitmap();
		}
	}

	private void startSurfaceRendering() {
		Log.d(TAG, "start surface rendering");
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.scheduleAtFixedRate(this, 100,
				INTERVAL_PERIOD, TimeUnit.MILLISECONDS);
	}

	public void resumeSurfaceRendering() {
		if (scheduledExecutorService != null
				&& !scheduledExecutorService.isShutdown()) {
			stopSurfaceRendering();
		}
		startSurfaceRendering();
	}

	private void stopSurfaceRendering() {
		Log.d(TAG, "stop surface rendering");
		scheduledExecutorService.shutdown();
		surfaceHolder.removeCallback(this);
	}

	private void initBallonView() {
		Bitmap konoatoBalloonBitmap = BitmapFactory.decodeResource(getContext()
				.getResources(), R.drawable.logo);
		konoatoBalloon = new BitmapBalloonView(konoatoBalloonBitmap);
		initKonoatoBalloonPosition();
	}

	private void initKonoatoBalloonPosition() {
		if (currentConfiguration == null) {
			resetBalloonPosition();
			return;
		}

		if (currentConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setBalloonPosition(portraitCanvasPosition);
		} else {
			setBalloonPosition(landscapeCanvasPosition);
		}
	}

	private void resetBalloonPosition() {
		if (currentCanvasSize == null) {
			return;
		}
		konoatoBalloon.move(
				currentCanvasSize.width / 10,
				(currentCanvasSize.height * 9 / 10)
						- konoatoBalloon.getHeight());
	}

	private void setBalloonPosition(CanvasPosition canvasPosition) {
		if (canvasPosition == null) {
			resetBalloonPosition();
		} else {
			konoatoBalloon.move(canvasPosition.x, canvasPosition.y);
		}
	}

	private void saveToPortraitBalloonPosition() {
		portraitCanvasPosition = new CanvasPosition(konoatoBalloon.getLeft(),
				konoatoBalloon.getTop());
	}

	private void saveToLandscapeBalloonPosition() {
		landscapeCanvasPosition = new CanvasPosition(konoatoBalloon.getLeft(),
				konoatoBalloon.getTop());
	}

	private float calculateBitmapScale(int bitmapWidth, int bitmapHeight,
			int canvasWidth, int canvasHeight) {
		float scale = (float) canvasWidth / (float) bitmapWidth;
		float scaledHeight = bitmapHeight * scale;
		if (scaledHeight > canvasHeight) {
			scale = (float) canvasHeight / (float) bitmapHeight;
		}
		return scale;
	}

	private void drawExtendedBitmap(Bitmap bitmap, Canvas canvas) {
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();

		float scale = calculateBitmapScale(bitmapWidth, bitmapHeight,
				canvasWidth, canvasHeight);

		extendedBitmapSize = new Size((int) (bitmapWidth * scale),
				(int) (bitmapHeight * scale));
		float blankX = ((extendedBitmapSize.width - canvasWidth) / scale) / 2;
		float blankY = ((extendedBitmapSize.height - canvasHeight) / scale) / 2;

		Rect srcRect = new Rect((int) blankX, (int) blankY,
				(int) ((canvasWidth / scale) + blankX),
				(int) ((canvasHeight / scale) + blankY));
		Rect dstRect = new Rect(0, 0, canvasWidth, canvasHeight);

		canvas.drawBitmap(bitmap, srcRect, dstRect, new Paint());
	}

	private boolean isCanvasSizeUpdateRequired(Size canvasSize) {
		if (currentCanvasSize == null) {
			return true;
		}
		if (!isConfigurationChanged) {
			return false;
		}
		return canvasSize.width != currentCanvasSize.width
				|| canvasSize.height != currentCanvasSize.height;
	}

	private void updateCanvasSize(Size canvasSize) {
		isConfigurationChanged = false;
		currentCanvasSize = canvasSize;
		initKonoatoBalloonPosition();
	}

	private boolean isKonoatoBallooonInclueded() {
		float blankLeft = (currentCanvasSize.width - extendedBitmapSize.width) / 2;
		float blankTop = (currentCanvasSize.height - extendedBitmapSize.height) / 2;
		float balloonLeftLimit = blankLeft + ((float) extendedBitmapSize.width)
				- konoatoBalloon.getWidth();
		float balloonTopLimit = blankTop + ((float) extendedBitmapSize.height)
				- konoatoBalloon.getHeight();

		boolean isIncluededX = konoatoBalloon.getLeft() >= blankLeft
				&& konoatoBalloon.getLeft() <= balloonLeftLimit;
		boolean isIncluededY = konoatoBalloon.getTop() >= blankTop
				&& konoatoBalloon.getTop() <= balloonTopLimit;
		Log.d(TAG, "isIncluededX => " + String.valueOf(isIncluededX)
				+ " isIncluededY => " + String.valueOf(isIncluededY));

		return isIncluededX && isIncluededY;
	}

	private Bitmap createCompositeBitmap() {
		Bitmap canvasBitmap = Bitmap.createBitmap(currentCanvasSize.width,
				currentCanvasSize.height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(canvasBitmap);
		renderToCanvas(canvas);
		return canvasBitmap;
	}

	private Bitmap createCompositeBitmpWithoutBlank() {
		float blankLeft = (currentCanvasSize.width - extendedBitmapSize.width) / 2;
		float blankTop = (currentCanvasSize.height - extendedBitmapSize.height) / 2;
		stopSurfaceRendering();
		konoatoBalloon.move(konoatoBalloon.getLeft() - blankLeft,
				konoatoBalloon.getTop() - blankTop);
		Bitmap canvasBitmap = Bitmap.createBitmap(extendedBitmapSize.width,
				extendedBitmapSize.height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(canvasBitmap);
		renderToCanvas(canvas);
		konoatoBalloon.move(konoatoBalloon.getLeft() + blankLeft,
				konoatoBalloon.getTop() + blankTop);
		startSurfaceRendering();
		return canvasBitmap;
	}

	private void renderToCanvas(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		Size scaledCanvasSize = new Size(canvas.getWidth(), canvas.getHeight());
		if (isCanvasSizeUpdateRequired(scaledCanvasSize)) {
			updateCanvasSize(scaledCanvasSize);
		}
		if (selectedBitmap != null) {
			drawExtendedBitmap(selectedBitmap, canvas);
		}
		canvas.drawBitmap(konoatoBalloon.getBitmap(), konoatoBalloon.getLeft(),
				konoatoBalloon.getTop(), new Paint());
	}

}
