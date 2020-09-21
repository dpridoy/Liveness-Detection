package com.dpridoy.livenessdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

    private final CameraService cameraService = new CameraService(this);

    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;
    Bitmap bitmap;
    boolean step2verification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextureView cameraTextureView = findViewById(R.id.textureView2);

        SurfaceTexture previewSurfaceTexture = cameraTextureView.getSurfaceTexture();
        if (previewSurfaceTexture != null) {
            // this first appears when we close the application and switch back - TextureView isn't quite ready at the first onResume.
            Surface previewSurface = new Surface(previewSurfaceTexture);

            cameraService.start(previewSurface);
            //analyzer.measurePulse(cameraTextureView, cameraService);
            Bitmap itmap=cameraTextureView.getBitmap();
            bitmap = Bitmap.createScaledBitmap(itmap,cameraTextureView.getWidth(),cameraTextureView.getHeight(),false);
            image=FirebaseVisionImage.fromBitmap(bitmap);

            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    excutefacedetection();
                }
            },10000);
        }

    }

    private void excutefacedetection() {
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
//                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
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

                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);

                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                    FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                    FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
                                    FirebaseVisionFaceLandmark leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                                    FirebaseVisionFaceLandmark bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                                    FirebaseVisionFaceLandmark rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                                    // Classification
                                    float smilingProbability = face.getSmilingProbability();
                                    String textsmile = "Happiness:" + smilingProbability;
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

                                    float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                                    String righteye = "righteye open:" + rightEyeOpenProbability;
                                    stringBuilder.append(righteye + "\n");
                                    //turning right
                                    Float right = face.getHeadEulerAngleY();
                                    if (right > 0 & step2verification) {
                                        Toast.makeText(MainActivity.this, "turned right", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }


                                    // Contours
                                    List<FirebaseVisionPoint> faceContours = face.getContour(FirebaseVisionFaceContour.FACE).getPoints();
                                    List<FirebaseVisionPoint> leftEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).getPoints();
                                    List<FirebaseVisionPoint> leftEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> rightEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).getPoints();
                                    List<FirebaseVisionPoint> rightEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> leftEyeContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                                    List<FirebaseVisionPoint> rightEyeContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints();
                                    List<FirebaseVisionPoint> upperLipTopContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).getPoints();
                                    List<FirebaseVisionPoint> upperLipBottomContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> lowerLipTopContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).getPoints();
                                    List<FirebaseVisionPoint> lowerLipBottomContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> noseBridgeContours = face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints();
                                    List<FirebaseVisionPoint> noseBottomContours = face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).getPoints();

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
    protected void onPause() {
        super.onPause();
        cameraService.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}