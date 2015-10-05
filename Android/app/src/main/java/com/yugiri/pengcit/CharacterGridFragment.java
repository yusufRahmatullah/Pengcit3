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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yugiri.tools.ImageGrid;
import com.yugiri.tools.PlatNomerTool;
import com.yugiri.tools.Tools;

import java.util.List;

/**
 * Created by gilang on 03/10/2015.
 */
public class CharacterGridFragment extends Fragment {
	private static int LOAD_IMAGE = 0;
	private ImageView image;
	private Button browseButton, processButton, learnButton;
	private TextView resultText;
	private Bitmap bitmap, gridImage;
	private EditText character;
	private CheckBox checkBox;
	private boolean isWhiteBackground;
	private Toolbar toolbar;
	private List<ImageGrid> grids;

	public CharacterGridFragment(){}

	public static CharacterGridFragment newInstance(){
		CharacterGridFragment fragment = new CharacterGridFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_character_grid, parent, false);
		toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
		image = (ImageView) v.findViewById(R.id.image);
		checkBox = (CheckBox) v.findViewById(R.id.checkbox);
		browseButton = (Button) v.findViewById(R.id.btn_select);
		processButton = (Button) v.findViewById(R.id.btn_process);
		learnButton = (Button) v.findViewById(R.id.btn_learn);
		character = (EditText) v.findViewById(R.id.character);
		resultText = (TextView) v.findViewById(R.id.result);


		checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isWhiteBackground = checkBox.isChecked();
			}
		});


		browseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images
						.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, LOAD_IMAGE);
			}
		});

		learnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {
					@Override
					protected Bitmap doInBackground(Bitmap... params) {
						params[0] = PlatNomerTool.scaleBitmap(params[0]);
						params[0] = Tools.blur(params[0], 5);
						params[0] = PlatNomerTool.getBinaryImage(params[0]);
						if(isWhiteBackground)
							params[0] = Tools.invertImage(params[0]);
						return params[0];
					}

					@Override
					protected void onPostExecute(Bitmap bitmap) {
						super.onPostExecute(bitmap);
						image.setImageBitmap(bitmap);
						Tools.learn(bitmap, character.getText().toString());
					}
				};
				task.execute(bitmap);
			}
		});

		processButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AsyncTask<Bitmap, Void, Bitmap> task = new AsyncTask<Bitmap, Void, Bitmap>() {
					@Override
					protected Bitmap doInBackground(Bitmap... params) {
						params[0] = PlatNomerTool.scaleBitmap(params[0]);
						params[0] = Tools.blur(params[0], 5);
						params[0] = PlatNomerTool.getBinaryImage(params[0]);
						if(isWhiteBackground)
							params[0] = Tools.invertImage(params[0]);
						gridImage = params[0].copy(params[0].getConfig(), true);
						grids = Tools.getGrids(params[0]);
						gridImage = Tools.drawGrid(gridImage, grids);
						return gridImage;
					}
					@Override
					protected void onPostExecute(Bitmap bitmap) {
						super.onPostExecute(bitmap);
						image.setImageBitmap(bitmap);
						resultText.setText(Tools.getStringInGrids(grids));
					}
				};
				task.execute(bitmap);
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

			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inMutable = true;
			bitmap = BitmapFactory.decodeFile(picturePath);
			image.setImageBitmap(bitmap);
		}
	}
}
