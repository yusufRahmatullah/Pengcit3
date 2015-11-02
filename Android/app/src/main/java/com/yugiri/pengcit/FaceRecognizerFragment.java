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

import com.yugiri.tools.FaceRecognizer;
import com.yugiri.tools.PlatNomerTool;

/**
 * Created by gilang on 02/11/2015.
 */
public class FaceRecognizerFragment extends Fragment {

	private static int LOAD_IMAGE;
	private Bitmap originalBitmap;
	private ImageView originalImage, homoDifImage, crossDifImage;
	private Button btnBrowse, btnProcess;
	private Toolbar toolbar;

	public FaceRecognizerFragment(){}

	public static FaceRecognizerFragment newInstance(){
		FaceRecognizerFragment fragment = new FaceRecognizerFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_face_recognizer, parent, false);
		originalImage = (ImageView) v.findViewById(R.id.img_original);
		homoDifImage = (ImageView) v.findViewById(R.id.img_homogen_diff);
		crossDifImage = (ImageView) v.findViewById(R.id.img_cross_diff);
		btnBrowse = (Button) v.findViewById(R.id.btn_select);
		btnProcess = (Button) v.findViewById(R.id.btn_process);
		toolbar = (Toolbar) v.findViewById(R.id.toolbar);

		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Face Recognition");

		btnBrowse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images
						.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, LOAD_IMAGE);
			}
		});

		btnProcess.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AsyncTask<Bitmap, Void, Bitmap[]> task = new AsyncTask<Bitmap, Void, Bitmap[]>() {

					@Override
					protected Bitmap[] doInBackground(Bitmap... params) {
						Bitmap homoBitmap = FaceRecognizer.homogenDifference(originalBitmap);
						Bitmap crossBitmap = FaceRecognizer.crossDifference(originalBitmap);
						Bitmap[] bitmaps = new Bitmap[2];
						bitmaps[0] = homoBitmap;
						bitmaps[1] = crossBitmap;
						return bitmaps;
					}

					@Override
					protected void onPostExecute(Bitmap[] bitmaps) {
						super.onPostExecute(bitmaps);
						homoDifImage.setImageBitmap(bitmaps[0]);
						crossDifImage.setImageBitmap(bitmaps[1]);
					}
				};

				task.execute(originalBitmap);
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

			originalBitmap = BitmapFactory.decodeFile(picturePath);
			originalImage.setImageBitmap(originalBitmap);
			originalBitmap = PlatNomerTool.scaleBitmap(originalBitmap);
		}
	}
}
