package com.dpridoy.livenessdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //private final CameraService cameraService = new CameraService(this);

    CameraKitView cameraKitView;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    Bitmap bitmap;
    boolean step2verification;
    Button capbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraKitView = findViewById(R.id.camera);
        capbtn = findViewById(R.id.btn);

        capbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA},
                            50);
                    decode();
                }

            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    50); }
    }

    private void decode() {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                cameraKitView.onStop();
                Bitmap itmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmap = Bitmap.createScaledBitmap(itmap, cameraKitView.getWidth(), cameraKitView.getHeight(), false);
                image = FirebaseVisionImage.fromBitmap(bitmap);
                excutefacedetection();
            }
        });
    }

    private void excutefacedetection() {
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
//                                    RectOverlay rect = new RectOverlay(graphicOverlay, face.getBoundingBox());
//                                    graphicOverlay.add(rect);

//                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
//                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
//                                    FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
//                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
//                                    FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
//                                    FirebaseVisionFaceLandmark leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
//                                    FirebaseVisionFaceLandmark bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
//                                    FirebaseVisionFaceLandmark rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                                    // Classification
                                    float smilingProbability = face.getSmilingProbability();
                                    String textsmile = "Happiness:" + smilingProbability;
                                    Log.e("Smile",textsmile);
                                    if (!step2verification) {
                                        if (smilingProbability > 0.5)
                                            //fragmentreplace("verified", "Verified with" + textsmile + "%");
                                            Log.e("Smile","Verified with" + textsmile + "%");
                                        else
                                            //fragmentreplace("key", "Smile PLease to verifiy");
                                            Log.e("Smile","Smile PLease to verifiy");
                                    }

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
                                    if (right > 0 & step2verification) {
                                        Toast.makeText(MainActivity.this, "turned right", Toast.LENGTH_SHORT).show();
                                        finish();
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

//                                    bottomsheetfragment bottomsheet = new bottomsheetfragment(stringBuilder.toString());
//                                    bottomsheet.show(getSupportFragmentManager(), "output");
//                                    showoutput.setText(stringBuilder.toString());
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

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}