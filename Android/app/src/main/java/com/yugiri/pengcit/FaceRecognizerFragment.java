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
import android.widget.Spinner;

import com.yugiri.tools.FaceRecognizer;
import com.yugiri.tools.PlatNomerTool;
import com.yugiri.tools.Tools;

/**
 * Created by gilang on 02/11/2015.
 */
public class FaceRecognizerFragment extends Fragment {

	private static int LOAD_IMAGE;
	private Bitmap originalBitmap;
	private ImageView originalImage, processedImage;
	private Button btnBrowse, btnProcess;
	private Spinner spinner;
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
		processedImage = (ImageView) v.findViewById(R.id.img_processed);
		btnBrowse = (Button) v.findViewById(R.id.btn_select);
		btnProcess = (Button) v.findViewById(R.id.btn_process);
		spinner = (Spinner) v.findViewById(R.id.spinner);
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
				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {
					int item = spinner.getSelectedItemPosition();

					@Override
					protected Bitmap doInBackground(Bitmap... params) {
						Bitmap output = Bitmap.createBitmap(params[0]);
						switch(item) {
							case 0:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
									.FIRST_ORDER, FaceRecognizer.SOBEL_OPERATOR);
								break;
							case 1:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
											.SECOND_ORDER, FaceRecognizer.SOBEL_OPERATOR);
								break;
							case 2:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.FIRST_ORDER, FaceRecognizer.SCHARR_OPERATOR);
								break;
							case 3:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
											.SECOND_ORDER, FaceRecognizer.SCHARR_OPERATOR);
								break;
							case 4:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.FIRST_ORDER,FaceRecognizer.PREWIT_OPERATOR);
								break;
							case 5:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.SECOND_ORDER,FaceRecognizer.PREWIT_OPERATOR);
								break;
							case 6:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.FIRST_ORDER,FaceRecognizer.ROBERT_CROSS_OPERATOR);
								break;
							case 7:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.FIRST_ORDER,FaceRecognizer.FREI_CHAN_OPERATOR);
								break;
							case 8:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.SECOND_ORDER, FaceRecognizer.FREI_CHAN_OPERATOR);
								break;
							case 9:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.FIRST_ORDER,FaceRecognizer.KIRSCH_OPERATOR);
								break;
							case 10:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.SECOND_ORDER,FaceRecognizer.KIRSCH_OPERATOR);
								break;
							case 11:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
									.DIAGONAL_LAPLACIAN);
								break;
							case 12:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
									.DIAMOND_LAPLACIAN_1);
								break;
							case 13:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
									.DIAMOND_LAPLACIAN_2);
								break;
							case 14:
								output = FaceRecognizer.edgeDetection(params[0], FaceRecognizer
									.FOUR_DIRECTIONAL_LAPLACIAN);
								break;
							case 15:
								output = FaceRecognizer.edgeDetection(params[0],
									FaceRecognizer.EIGHT_DIRECTIONAL_LAPLACIAN);
								break;
						}
						output = PlatNomerTool.getBinaryImage(output);
						output = Tools.invertImage(output);
						output = FaceRecognizer.clusterFace(output, null);

						return output;
					}

					@Override
					protected void onPostExecute(Bitmap bitmaps) {
						super.onPostExecute(bitmaps);
						processedImage.setImageBitmap(bitmaps);
					}
				};

				task.execute(originalBitmap);
			}
		});

		String[] objs = {"First Order Sobel", "Second Order Sobel", "First Order Scharr", "Second" +
				" Order Scharr", "First Order Prewit", "Second Order Prewit", "First Order Robert" +
				" Cross", "First Order Frei-Chan", "Second Order Frei-Chan", "First Order" +
				" Kirsch", "Second Order Kirsch", "Diagonal Laplacian", "Diamond Laplacian 1",
				"Diamond Laplacian 2"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout
				.simple_spinner_dropdown_item, objs);
		spinner.setAdapter(adapter);

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
