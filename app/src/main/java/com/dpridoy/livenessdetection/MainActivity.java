package com.dpridoy.livenessdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK;

public class MainActivity extends AppCompatActivity {

    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    Bitmap bitmap;
    FloatingActionButton capbtn;
    CameraView cameraView;
    int counter=0;
    TextView step1, step2, step3, step4, step5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera);
        capbtn = findViewById(R.id.btn);
        step1=findViewById(R.id.txtStep1);
        step2=findViewById(R.id.txtStep2);
        step3=findViewById(R.id.txtStep3);
        step4=findViewById(R.id.txtStep4);
        step5=findViewById(R.id.txtStep5);

        capbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                counter++;
                if (counter==6){
                    counter=0;
                    step1.setText("Step 1: Please smile");
                    step2.setText("Step 2: Please close your both eyes");
                    step3.setText("Step 3: Please close your right eye");
                    step4.setText("Step 4: Please turn your head little right");
                    step5.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.switchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.getFacing()==FACING_BACK){
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                }else{
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                }

            }
        });

        cameraView.start();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                bitmap=cameraKitImage.getBitmap();
                bitmap=Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                //cameraView.stop();
                excutefacedetection(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }

    private void excutefacedetection(final Bitmap bitmap) {
        image =FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
//                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);
        Task<List<FirebaseVisionFace>> result = detector.detectInImage(image)

                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
//                                progressBar.setVisibility(View.GONE);

                                if (faces.size() == 0) {
                                    Toast.makeText(MainActivity.this, "Face not Found", Toast.LENGTH_SHORT).show();
                                }

                                for (FirebaseVisionFace face : faces) {
                                    //Toast.makeText(MainActivity.this, "reached", Toast.LENGTH_SHORT).show();

                                    // Landmarks

                                    StringBuilder stringBuilder = new StringBuilder();

                                    // Classification

                                    //smiling test
                                    if (counter==1){
                                        float smilingProbability = face.getSmilingProbability();
                                        String textsmile = "Happiness:" + smilingProbability;
                                        Log.e("Smile",textsmile);
                                        if (smilingProbability>0.5){
                                            step1.setText("Step 1: Done");
                                        }else {
                                            step1.setText("Step 1: Please smile");
                                            Toast.makeText(MainActivity.this, "Please try again from start", Toast.LENGTH_SHORT).show();
                                            counter=0;
                                        }

                                    }

                                    if (counter==2){
                                        float leftEyeOpenProbability = face.getLeftEyeOpenProbability();
                                        String lefteye = "lefteye open:" + leftEyeOpenProbability;
                                        stringBuilder.append(lefteye + "\n");
                                        Log.e("Left Eye",lefteye);

                                        float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                                        String righteye = "righteye open:" + rightEyeOpenProbability;
                                        stringBuilder.append(righteye + "\n");
                                        Log.e("Right Eye",righteye);

                                        if (leftEyeOpenProbability<0.5 && rightEyeOpenProbability<0.5){
                                            step2.setText("Step 2: Done");
                                        }else{
                                            step1.setText("Step 1: Please smile");
                                            step2.setText("Step 2: Please close your both eyes");
                                            Toast.makeText(MainActivity.this, "Please try again from start", Toast.LENGTH_SHORT).show();
                                            counter=0;
                                        }
                                    }

                                    if (counter==3){
                                        float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                                        String righteye = "righteye open:" + rightEyeOpenProbability;
                                        stringBuilder.append(righteye + "\n");
                                        Log.e("Right Eye",righteye);

                                        if (rightEyeOpenProbability<0.5){
                                            step3.setText("Step 3: Done");
                                        }else {
                                            step1.setText("Step 1: Please smile");
                                            step2.setText("Step 2: Please close your both eyes");
                                            step3.setText("Step 3: Please close your right eye");
                                            Toast.makeText(MainActivity.this, "Please try again from start", Toast.LENGTH_SHORT).show();
                                            counter=0;
                                        }
                                    }

                                    if (counter==4){
                                        //turning right
                                        Float right = face.getHeadEulerAngleY();
                                        Log.e("Turn Right",right.toString());

                                        if (right>10){
                                            step4.setText("Step 4: Done");
                                            step5.setVisibility(View.VISIBLE);
                                        }else {
                                            step1.setText("Step 1: Please smile");
                                            step2.setText("Step 2: Please close your both eyes");
                                            step3.setText("Step 3: Please close your right eye");
                                            step4.setText("Step 4: Please turn your head little right");
                                            Toast.makeText(MainActivity.this, "Please try again from start", Toast.LENGTH_SHORT).show();
                                            counter=0;
                                        }
                                    }

                                    if (counter==5){
                                        cameraView.stop();
//                                        counter++;
                                        Toast.makeText(MainActivity.this, "Image saved in LivenessDetection folder", Toast.LENGTH_SHORT).show();
                                        saveImage(bitmap);
                                    }






                                    // Contours
//                                    List<FirebaseVisionPoint> faceContours = face.getContour(FirebaseVisionFaceContour.FACE).getPoints();
//                                    List<FirebaseVisionPoint> leftEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).getPoints();
//                                    List<FirebaseVisionPoint> leftEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).getPoints();
//                                    List<FirebaseVisionPoint> rightEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).getPoints();
//                                    List<FirebaseVisionPoint> rightEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).getPoints();
//                                    List<FirebaseVisionPoint> leftEyeContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
//                                    List<FirebaseVisionPoint> rightEyeContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints();
//                                    List<FirebaseVisionPoint> upperLipTopContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).getPoints();
//                                    List<FirebaseVisionPoint> upperLipBottomContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
//                                    List<FirebaseVisionPoint> lowerLipTopContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).getPoints();
//                                    List<FirebaseVisionPoint> lowerLipBottomContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).getPoints();
//                                    List<FirebaseVisionPoint> noseBridgeContours = face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints();
//                                    List<FirebaseVisionPoint> noseBottomContours = face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).getPoints();

                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

    }

    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/LivenessDetection");
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "LD"+ timeStamp +".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}