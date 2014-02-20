package org.myatsumoto.mechakucha_sex;

public interface BalloonView {

	boolean isTouched(float x, float y);

	void move(float top, float left);

	float getTop();

	float getLeft();

	float getWidth();

	float getHeight();

}
