package org.myatsumoto.mechakucha_sex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_ACTION_PICK = 1;

	private static final String APP_DIRECTORY_NAME = "mchkch";

	// private CameraSurface cameraSurface;

	private BalloonSurface balloonSurface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpSurfaceViews();
		setUpButtons();
	}

	@Override
	protected void onResume() {
		super.onResume();
		balloonSurface.resumeSurfaceRendering();
	}

	private void setUpButtons() {
		Button button = (Button) findViewById(R.id.camera_shutter_button);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestGalleryIntent();
			}
		});

		Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bitmap compositeBitmap = balloonSurface
						.requestCompositeBitmap();
				saveCompositeBitmap(compositeBitmap);
			}
		});
	}

	private boolean isSDCardReadyWritable() {
		String state = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(state));
	}

	private File createAppDirectory() {
		File file = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + APP_DIRECTORY_NAME + "/");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	private void saveCompositeBitmap(Bitmap bitmap) {
		if (!isSDCardReadyWritable()) {
			Toast.makeText(this, "SDCARDが認識されません。", Toast.LENGTH_SHORT).show();
			return;
		}
		File appDirectory = createAppDirectory();
		File saveFile = new File(appDirectory.getAbsolutePath() + "/"
				+ System.currentTimeMillis() + ".jpg");

		try {
			FileOutputStream out = new FileOutputStream(
					saveFile.getAbsolutePath());
			bitmap.compress(CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			Toast.makeText(this, "保存されました。", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "エラーが発生しました。", Toast.LENGTH_SHORT).show();
		}

		registAndroidDatabase(saveFile.getAbsolutePath());
	}

	private void requestGalleryIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_ACTION_PICK);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_ACTION_PICK) {
			try {
				InputStream is = getContentResolver().openInputStream(
						data.getData());
				Bitmap selectedBitmap = BitmapFactory.decodeStream(is);
				balloonSurface.setSelectedBitmap(selectedBitmap);
				is.close();
			} catch (IOException e) {
				Log.d("TAG", "selected Bitmap => " + e.getMessage());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		balloonSurface.onConfigurationChanged(newConfig);
	}

	private void setUpSurfaceViews() {
		balloonSurface = new BalloonSurface(this);
		FrameLayout balloonSurfaceContainer = (FrameLayout) findViewById(R.id.balloon_surface_container);
		balloonSurfaceContainer.addView(balloonSurface);
	}

	private void registAndroidDatabase(String path) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Images.Media.MIME_TYPE, "image/jpeg");
		contentValues.put("_data", path);
		getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
