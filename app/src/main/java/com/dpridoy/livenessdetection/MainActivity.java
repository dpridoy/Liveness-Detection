package com.dpridoy.livenessdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    Bitmap bitmap;
    FloatingActionButton capbtn;
    CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera);
        capbtn = findViewById(R.id.btn);

        capbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

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

    private void excutefacedetection(Bitmap bitmap) {
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
                                    Toast.makeText(MainActivity.this, "reached", Toast.LENGTH_SHORT).show();

                                    // Landmarks

                                    StringBuilder stringBuilder = new StringBuilder();

                                    // Classification
                                    float smilingProbability = face.getSmilingProbability();
                                    String textsmile = "Happiness:" + smilingProbability;
                                    Log.e("Smile",textsmile);

                                    stringBuilder.append(textsmile + "\n");

                                    float leftEyeOpenProbability = face.getLeftEyeOpenProbability();
                                    String lefteye = "lefteye open:" + leftEyeOpenProbability;
                                    stringBuilder.append(lefteye + "\n");
                                    Log.e("Left Eye",lefteye);

                                    float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                                    String righteye = "righteye open:" + rightEyeOpenProbability;
                                    stringBuilder.append(righteye + "\n");
                                    Log.e("Right Eye",righteye);
                                    //turning right
                                    Float right = face.getHeadEulerAngleY();
                                    Log.e("Turn Right",right.toString());


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
}