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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.yugiri.tools.FaceRecognizer;
import com.yugiri.tools.FacesLocationDetector;
import com.yugiri.tools.PlatNomerTool;
import com.yugiri.tools.Tools;

/**
 * Created by gilang on 18/11/2015.
 */
public class SkinDetectorFragment extends Fragment {

	private static int LOAD_IMAGE;
	private Bitmap originalBitmap;
	private ImageView originalImage, processedImage;
	private Button btnBrowse, btnProcess;
	private Spinner spinner;
	private Toolbar toolbar;
	private ProgressBar progressBar;

	public SkinDetectorFragment(){}

	public static SkinDetectorFragment newInstance(){
		SkinDetectorFragment fragment = new SkinDetectorFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_face_recognizer, parent, false);
		originalImage = (ImageView) v.findViewById(R.id.img_original);
		processedImage = (ImageView) v.findViewById(R.id.img_processed);
		btnBrowse = (Button) v.findViewById(R.id.btn_select);
		btnProcess = (Button) v.findViewById(R.id.btn_process);
		spinner = (Spinner) v.findViewById(R.id.spinner);
		toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		progressBar = (ProgressBar) v.findViewById(R.id.progressbar);

		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
		((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Skin Detection");

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
				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {
					int item = spinner.getSelectedItemPosition();

					@Override
					protected Bitmap doInBackground(Bitmap... params) {
						Bitmap output = Bitmap.createBitmap(params[0]);
						output = FacesLocationDetector.skinDetection(output, 300, getActivity());
						return output;
					}

					@Override
					protected void onPostExecute(Bitmap bitmaps) {
						super.onPostExecute(bitmaps);
						progressBar.setVisibility(View.GONE);
						processedImage.setImageBitmap(bitmaps);
					}
				};
				progressBar.setVisibility(View.VISIBLE);
				task.execute(originalBitmap);
			}
		});

		spinner.setVisibility(View.GONE);

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
