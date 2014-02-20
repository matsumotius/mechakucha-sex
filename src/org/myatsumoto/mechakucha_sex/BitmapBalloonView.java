package org.myatsumoto.mechakucha_sex;

import android.graphics.Bitmap;

public class BitmapBalloonView implements BalloonView {

	private Bitmap bitmap;

	private float width;

	private float height;

	private float top = 0;

	private float left = 0;

	public BitmapBalloonView(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	@Override
	public void move(float left, float top) {
		this.left = left;
		this.top = top;
	}

	@Override
	public float getTop() {
		return top;
	}

	@Override
	public float getLeft() {
		return left;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public boolean isTouched(float x, float y) {
		return (x >= left && x <= left + width)
				&& (y >= top && y <= top + height);
	}

}
