package com.yugiri.pengcit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yugiri.tools.ChaincodeImage;
import com.yugiri.tools.PlatNomerTool;

/**
 * Created by gilang on 23/09/2015.
 */
public class PlatNomerFragment extends Fragment{
	private static int LOAD_IMAGE = 0;
	private ImageView image;
	private Button button;
	private Bitmap bitmap;
	private ChaincodeImage chaincodeImage;
	private Toolbar toolbar;
	private TextView text;
	private int number;

	public PlatNomerFragment(){}

	public static PlatNomerFragment newInstance(){
		PlatNomerFragment fragment = new PlatNomerFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_number_recognizer, parent, false);
		toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
		text = (TextView) v.findViewById(R.id.text);
		image = (ImageView) v.findViewById(R.id.image);
		button = (Button) v.findViewById(R.id.btn_select);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images
						.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, LOAD_IMAGE);
			}
		});
		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null){
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getActivity().getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			bitmap = BitmapFactory.decodeFile(picturePath);

			AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(Bitmap... params) {
					bitmap = PlatNomerTool.scaleBitmap(bitmap);
					bitmap = PlatNomerTool.getBinaryImage(bitmap);
					return bitmap;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap) {
					super.onPostExecute(bitmap);
					image.setImageBitmap(bitmap);
				}
			};
			task.execute(bitmap);

			image.setImageBitmap(bitmap);
			image.setVisibility(View.VISIBLE);
//			int width = bitmap.getWidth();
//			int height = bitmap.getHeight();
//			int[] pixels = new int[width * height];
//			bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//			chaincodeImage = new ChaincodeImage(width, height, pixels);
//			try {
//				number = chaincodeImage.recognize();
//				text.setText("Recognized number : " + number);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
}
