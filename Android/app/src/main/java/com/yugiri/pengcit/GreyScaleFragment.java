package com.yugiri.pengcit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
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
import android.widget.SeekBar;

import com.yugiri.tools.AndroidEqualizer;

/**
 * Created by gilang on 06/09/2015.
 */
public class GreyScaleFragment extends Fragment {

	private static int LOAD_IMAGE;
	private Toolbar toolbar;
	private SeekBar seekbar;
	private ImageView image;
	private ImageView image2;
	private Button button;
	private Bitmap originalBitmap;

	public static GreyScaleFragment newInstance(){
		GreyScaleFragment fragment = new GreyScaleFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.fragment_greyscale, parent, false);
		toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		seekbar = (SeekBar) v.findViewById(R.id.seekBar);
		image = (ImageView) v.findViewById(R.id.image);
		image2 = (ImageView) v.findViewById(R.id.image2);
		button = (Button) v.findViewById(R.id.btn_select);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images
						.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, LOAD_IMAGE);
			}
		});

		((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(originalBitmap != null){
					//Bitmap greyscaled = AndroidEqualizer.getGrayScaledBitmap(originalBitmap);
					Bitmap equalized = AndroidEqualizer.getEqualizedBitmap(originalBitmap,
							progress * 10000);
					//image.setImageBitmap(greyscaled);
					image2.setImageBitmap(equalized);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

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
			//Bitmap greyscaled = AndroidEqualizer.getGrayScaledBitmap(originalBitmap);
			Bitmap equalized = AndroidEqualizer.getEqualizedBitmap(originalBitmap,0);
			image.setImageBitmap(originalBitmap);
			image2.setImageBitmap(equalized);

		}
	}


}
