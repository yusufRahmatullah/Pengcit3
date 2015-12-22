package com.yugiri.pengcit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
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

import com.yugiri.tools.Box;
import com.yugiri.tools.FaceRecognizer;
import com.yugiri.tools.FacesLocationDetector;
import com.yugiri.tools.PlatNomerTool;
import com.yugiri.tools.Point;
import com.yugiri.tools.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by macair on 12/22/15.
 */
public class FaceRecognizerFinalFragment extends Fragment {

    private static int LOAD_IMAGE;
    private Bitmap originalBitmap;
    private ImageView originalImage, processedImage;
    private Button btnBrowse, btnProcess;
    private Spinner spinner;
    private Toolbar toolbar;
    private ViewGroup container;
    private List<Box> boxes;
    private List<Bitmap> bitmapList;
    private List<Bitmap> originalBitmaps;
    private List<Bitmap> edgeBitmap;
    private List<Bitmap> resultBitmap;

    public FaceRecognizerFinalFragment(){}

    public static FaceRecognizerFinalFragment newInstance(){
        FaceRecognizerFinalFragment fragment = new FaceRecognizerFinalFragment();
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_face_recognizer_final, parent, false);
        originalImage = (ImageView) v.findViewById(R.id.img_original);
        processedImage = (ImageView) v.findViewById(R.id.img_processed);
        btnBrowse = (Button) v.findViewById(R.id.btn_select);
        btnProcess = (Button) v.findViewById(R.id.btn_process);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        spinner = (Spinner) v.findViewById(R.id.spinner);
        container = (ViewGroup) v.findViewById(R.id.image_container);
        spinner.setVisibility(View.GONE);

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
//                        output = FacesLocationDetector.skinDetection(output, 300, getActivity());
                        boxes = new ArrayList<>();
                        List<List<Point>> centroidsList = new ArrayList<List<Point>>();
                        FaceDetector detector = new FaceDetector(output.getWidth(), output.getHeight(), 20);
                        FaceDetector.Face[] faces = new FaceDetector.Face[20];
                        detector.findFaces(output, faces);
                        for(FaceDetector.Face f : faces){
                            if(f != null) {
                                List<Point> centroids = new ArrayList<>();
                                PointF point = new PointF();
                                f.getMidPoint(point);
                                int minx = (int) (point.x - f.eyesDistance());
                                if(minx < 0)
                                    minx = 0;
                                int maxx = (int) (point.x + f.eyesDistance());
                                if(maxx >= output.getWidth())
                                    maxx = output.getWidth() - 1;
                                int miny = (int) (point.y - f.eyesDistance());
                                if(miny < 0)
                                    miny = 0;
                                int maxy = (int) (point.y + (1.6f * f.eyesDistance()));
                                if(maxy >= output.getHeight())
                                    maxy = output.getHeight() - 1;
                                boxes.add(new Box(minx, maxx, miny, maxy));

                                int leftEye = (int) (point.x - (f.eyesDistance()/2));
                                if(leftEye < 0)
                                    leftEye = 0;
                                int rightEye = (int) (point.x + (f.eyesDistance()/2));
                                if(rightEye > output.getWidth())
                                    rightEye = output.getWidth() - 1;
                                int nose = (int) (point.y + (1/1.6f * f.eyesDistance()));
                                if(nose > output.getHeight())
                                    nose = output.getHeight() - 1;
                                int mouth = (int) (point.y + (1.6f * f.eyesDistance()));
                                if(mouth > output.getHeight())
                                    mouth = output.getHeight() - 1;
                                centroids.add(new Point(leftEye - minx, (int)point.y));
                                centroids.add(new Point(rightEye - minx, (int)point.y));
                                centroids.add(new Point((int)point.x, nose - miny));
                                centroids.add(new Point((int)point.x, mouth - miny));
                                centroidsList.add(centroids);
                            }
                        }
                        output = Tools.drawBoxes(output, boxes);
                        originalBitmaps = Tools.getSubImage(params[0], boxes);
                        edgeBitmap = new ArrayList<Bitmap>();
                        resultBitmap = new ArrayList<Bitmap>();
                        int counter = 0;
                        for(int i=0; i<originalBitmaps.size(); i++){
                            Bitmap b = originalBitmaps.get(i);
                            b = FaceRecognizer.edgeDetection(b, FaceRecognizer.FIRST_ORDER, FaceRecognizer.SCHARR_OPERATOR);
                            b = PlatNomerTool.getBinaryImage(b);
                            edgeBitmap.add(b);
                            b = Tools.invertImage(b);
                            b = FaceRecognizer.clusterFace(b, null);
                            resultBitmap.add(b);
                            counter++;
                        }
                        return output;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        originalImage.setImageBitmap(bitmap);
                        container.removeAllViews();
                        for(int i=0; i<originalBitmaps.size(); i++){
                            View v = inflater.inflate(R.layout.imageview, container, false);
                            ImageView image1 = (ImageView) v.findViewById(R.id.image1);
                            ImageView image2 = (ImageView) v.findViewById(R.id.image2);
                            ImageView image3 = (ImageView) v.findViewById(R.id.image3);
                            image1.setImageBitmap(originalBitmaps.get(i));
                            image2.setImageBitmap(edgeBitmap.get(i));
                            image3.setImageBitmap(resultBitmap.get(i));
                            container.addView(v);
                        }
                    }
                };

                task.execute(originalBitmap);
            }
        });

        return v;
    }

    public List<Point> getCentroids(Bitmap b){
        List<Point> centroids = new ArrayList<>();
        FaceDetector detector = new FaceDetector(b.getWidth(), b.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        detector.findFaces(b, faces);
        FaceDetector.Face f = faces[0];
        if(f != null) {
            PointF point = new PointF();
            f.getMidPoint(point);
            int leftEye = (int) (point.x - f.eyesDistance());
            if(leftEye < 0)
                leftEye = 0;
            int rightEye = (int) (point.x + f.eyesDistance());
            if(rightEye > b.getWidth())
                rightEye = b.getWidth() - 1;
            int nose = (int) (point.y + (1/1.618 * f.eyesDistance()));
            if(nose > b.getHeight())
                nose = b.getHeight() - 1;
            int mouth = (int) (point.y + (1.618 * f.eyesDistance()));
            if(mouth > b.getHeight())
                mouth = b.getHeight() - 1;
            centroids.add(new Point((int) point.x, nose));
            centroids.add(new Point((int) point.x, mouth));
            centroids.add(new Point(leftEye, (int)point.y));
            centroids.add(new Point(rightEye, (int)point.y));
        }
        return centroids;
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

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.RGB_565;
            originalBitmap = BitmapFactory.decodeFile(picturePath, op);
            originalImage.setImageBitmap(originalBitmap);
//            originalBitmap = PlatNomerTool.scaleBitmap(originalBitmap);
        }
    }
}
