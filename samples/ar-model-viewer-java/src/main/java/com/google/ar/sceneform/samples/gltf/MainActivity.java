package com.google.ar.sceneform.samples.gltf;

import static android.app.PendingIntent.getActivity;
import static com.google.ar.sceneform.rendering.HeadlessEngineWrapper.TAG;
import static com.google.mlkit.vision.pose.PoseDetectorOptionsBase.CPU_GPU;
//import android.graphics.Color;  // For Color.RED, Color.TRANSPARENT
import android.graphics.PorterDuff;  // For PorterDuff.Mode.CLEAR

import com.google.ar.core.Trackable;
import com.google.ar.sceneform.math.Vector3;

import android.graphics.Paint;  // For Paint
import android.graphics.Canvas;  // For Canvas
import android.graphics.Bitmap;  // For Bitmap
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import com.google.ar.core.Camera;
import com.google.ar.core.Coordinates2d;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.ux.VideoNode;
import com.google.ar.sceneform.rendering.Color;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.graphics.Matrix;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements
        FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{
//        ImageReader.OnImageAvailableListener{
    private long startTime;
    private TransformableNode mainVideoNode;
    private InputImage inputImage;
    private static final long WAIT_TIME_MS = 5000; // 5 seconds
    private Handler handler = new Handler();
    private boolean isCPRScheduledForRemoval = false;
    private TransformableNode chestModelNode;
    private TransformableNode currentModelNode;
    private boolean hasAnchored = false;
    private ImageAnalysis imageAnalysis;
    private ProcessCameraProvider cameraProvider;
    private Anchor currentAnchor = null;
    private AnchorNode anchorNode = null;
    private TransformableNode modelNode = null;
    private PreviewView previewView;
    private Camera camera;
    ArrayList<Pose> poseArrayList = new ArrayList<>();
    ArrayList<Pose> poseArrayListNew = new ArrayList<>();
    // Declare a timestamp variable to track the last update time
    private long lastPoseUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 1000; // 1 second in milliseconds
    private ImageReader imageReader;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private final List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private int mode = R.id.menuPlainVideo;
    private String userQueryFromSpeech;
    private Image rawDepth;
    private Size mPreviewSize;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private TextView ttsTextView;
    private TextView geminiResponse;
    private Bitmap latestBitmap;
    private ImageView micButton;
    private Bitmap bitmap;
    private Canvas canvas;
    private TextureView textureView;
    private ArFragment arFragment;
    //person performing cpr
    private Renderable model;
    //person checking breathing
    private Renderable model2;
    TransformableNode cpr;
    //spot location arrow
    private Renderable model3;

    //dot for neck
    private Renderable model4;
    //circle for chest
    private Renderable model5;
    private ViewRenderable viewRenderable;
    private Switch arCoreSwitch;
    private DrawerLayout drawerLayout;
    private boolean isListening = false;
    private LinearLayout chatBodyContainer;
    private ChatFutures chatModel;
    private HandlerThread handlerThread;
    private boolean arMode = false; // Track whether we're in AR mode
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private TransformableNode cprGuy(Frame frame, Session session) {
        Anchor newMarkAnchor = session.createAnchor(
                frame.getCamera().getPose()
                        .compose(com.google.ar.core.Pose.makeTranslation(0, -0.2f, -1.4f))
                        .extractTranslation());
        AnchorNode anchorNode = new AnchorNode(newMarkAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model)
                .animate(true).start();
        model.select();

        return model;
    }
    private void mainVideoNode(Frame frame, Session session) {
        // Create the transformable model and add it to the anchor.
        Anchor newMarkAnchor = session.createAnchor(
                frame.getCamera().getPose()
                        .compose(com.google.ar.core.Pose.makeTranslation(0, 0.3f, -3f))
                        .extractTranslation());
        TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
        AnchorNode anchorNode = new AnchorNode(newMarkAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        modelNode.setWorldScale(new Vector3(1.4f, 1.4f, 1.4f));
        modelNode.setParent(anchorNode);

        final int rawResId;
        final Color chromaKeyColor;
        if (mode == R.id.menuPlainVideo) {
            rawResId = R.raw.mainvidnode;
            chromaKeyColor = null;
        } else {
            rawResId = R.raw.mainvidnode;
            chromaKeyColor = new Color(0.1843f, 1.0f, 0.098f);
        }

        MediaPlayer player = MediaPlayer.create(this, rawResId);
        player.setLooping(false);  // Set looping to false, so it only plays once.
        player.start();  // Start playing the video
        mediaPlayers.add(player);

        VideoNode videoNode = new VideoNode(this, player, chromaKeyColor, new VideoNode.Listener() {
            @Override
            public void onCreated(VideoNode videoNode) {
            }

            @Override
            public void onError(Throwable throwable) {
                Toast.makeText(MainActivity.this, "Unable to load material", Toast.LENGTH_LONG).show();
            }
        });
        videoNode.setParent(modelNode);

        // Optionally keep the video node always facing the camera
        // videoNode.setRotateAlwaysToCamera(true);

        modelNode.select();

        // Start the checkBreathing method and remove its model after 8 seconds
        TransformableNode breathingModelNode = checkBreathing(frame, session);

        // Schedule the destruction of the breathing model after 8 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (breathingModelNode != null && breathingModelNode.getParent() != null) {
                breathingModelNode.setParent(null);  // Remove the breathing model node from the scene
                Log.d("BreathingModel", "Breathing model destroyed after 8 seconds.");
            }
        }, 8000);  // 8 seconds in milliseconds

        // Schedule the destruction of the video node after 64 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            modelNode.setParent(null);  // Remove the video node from the scene
            Log.d("VideoNode", "Video node destroyed after 64 seconds.");
        }, 64000);  // 64 seconds in milliseconds

        // Schedule the creation and destruction of the cprGuy model
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TransformableNode cprGuyNode = cprGuy(frame, session);

            // Schedule the destruction of the cprGuy model 15 seconds after it is created
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (cprGuyNode != null && cprGuyNode.getParent() != null) {
                    cprGuyNode.setParent(null);  // Remove the cprGuy model node from the scene
                    Log.d("CPRGuyModel", "CPR Guy model destroyed after 15 seconds.");
                }
            }, 10000);  // 10 seconds in milliseconds

        }, 28000);  // 28 seconds in milliseconds
        // Schedule the creation and destruction of the placeChest model
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            placeChest(frame, session);

            // Schedule the destruction of the placeChest model 25 seconds after it is created
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Here, you will need to handle the removal of the placeChest model.
                // You should maintain a reference to the created anchorNode or modelNode for removal.
                // Assuming `chestModelNode` is a reference to the created model node, use:
                if (chestModelNode != null && chestModelNode.getParent() != null) {
                    chestModelNode.setParent(null);  // Remove the chest model node from the scene
                    Log.d("PlaceChestModel", "Place Chest model destroyed after 25 seconds.");
                }
            }, 25000);  // 25 seconds in milliseconds

        }, 38000);  // 38 seconds in milliseconds
    }


    private TransformableNode checkBreathing(Frame frame, Session session) {
        Anchor newMarkAnchor = session.createAnchor(
                frame.getCamera().getPose()
                        .compose(com.google.ar.core.Pose.makeTranslation(0, -0.2f, -1.4f))
                        .extractTranslation());
        AnchorNode anchorNode = new AnchorNode(newMarkAnchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(this.model2)
                .animate(true).start();
        model.select();

        return model;
    }


    private void placeChest(Frame frame, Session session) {
        if (!poseArrayListNew.isEmpty()) {
            Log.d("pose array", "is not empty");

            Pose pose = poseArrayListNew.get(0);
            PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
            PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
            PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
            PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
            if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
                float chestX = (leftShoulder.getPosition().x + rightShoulder.getPosition().x + leftHip.getPosition().x + rightHip.getPosition().x) / 4;
                float chestY = (leftShoulder.getPosition().y + rightShoulder.getPosition().y + leftHip.getPosition().y + rightHip.getPosition().y) / 4 * 0.75f + (leftShoulder.getPosition().y + rightShoulder.getPosition().y) / 4 * 0.25f;

                float u = (float) chestX / (float) frame.getCamera().getImageIntrinsics().getImageDimensions()[0];
                float v = (float) chestY / (float) frame.getCamera().getImageIntrinsics().getImageDimensions()[1];

                try {
                    Image rawDepth = null;
                    if (frame.acquireRawDepthImage16Bits() != null) {
                        rawDepth = frame.acquireRawDepthImage16Bits();
                        // Proceed with processing the rawDepth image
                    } else {
                        Log.e("DepthImage", "Depth image not available yet.");
                    }
                    float[] cpuCoordinates = new float[] {chestX, chestY};
                    float[] textureCoordinates = new float[2];
                    frame.transformCoordinates2d(
                            Coordinates2d.VIEW,
                            cpuCoordinates,
                            Coordinates2d.TEXTURE_NORMALIZED,
                            textureCoordinates);

                    int depthX = (int) (textureCoordinates[0] * rawDepth.getWidth());
                    int depthY = (int) (textureCoordinates[1] * rawDepth.getHeight());
                    int z = getMillimetersDepth(rawDepth, depthX, depthY);
                    float depthZ = z / 1000.0f;

                    Ray ray = arFragment.getArSceneView().getScene().getCamera().screenPointToRay(chestX, chestY);
                    float[] intersection = computeRayPlaneIntersection(ray, depthZ);

                    if (intersection != null) {
                        com.google.ar.core.Pose landmarkPose = com.google.ar.core.Pose.makeTranslation(intersection[0], intersection[1], intersection[2]);
                        Anchor anchor = session.createAnchor(landmarkPose);
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        chestModelNode = new TransformableNode(arFragment.getTransformationSystem());
                        chestModelNode.setParent(anchorNode);
                        chestModelNode.setRenderable(this.model5)
                                .animate(true).start();
                        chestModelNode.select();

                        // The removal of this model is handled in mainVideoNode after 25 seconds
                    } else {
                        Log.e("DepthImage", "Depth image acquisition failed.");
                    }

                } catch (Exception e) {
                    Log.e("AnchorCreation", "Exception during anchor creation", e);
                }
            } else {
                Log.e("pose landmarks", "One or more landmarks are null");
            }
        } else {
            Log.d("pose array", "is empty");
        }
    }
    private void placeSpot(Frame frame, Session session) {
        if (!poseArrayListNew.isEmpty()) {
            Log.d("pose array", "is not empty");

            Pose pose = poseArrayListNew.get(0);
            PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);


            if (rightHip != null) {
                float rightHipX = rightHip.getPosition().x;
                float rightHipY = rightHip.getPosition().y;

                float u = (float) rightHipX / (float) frame.getCamera().getImageIntrinsics().getImageDimensions()[0];
                float v = (float) rightHipY / (float) frame.getCamera().getImageIntrinsics().getImageDimensions()[1];

                try {
                    Image rawDepth = null;
                    if (frame.acquireRawDepthImage16Bits() != null) {
                        rawDepth = frame.acquireRawDepthImage16Bits();
                        // Proceed with processing the rawDepth image
                    } else {
                        Log.e("DepthImage", "Depth image not available yet.");
                    }
                    float[] cpuCoordinates = new float[] {rightHipX, rightHipY};
                    float[] textureCoordinates = new float[2];
                    frame.transformCoordinates2d(
                            Coordinates2d.VIEW,
                            cpuCoordinates,
                            Coordinates2d.TEXTURE_NORMALIZED,
                            textureCoordinates);

                    int depthX = (int) (textureCoordinates[0] * rawDepth.getWidth());
                    int depthY = (int) (textureCoordinates[1] * rawDepth.getHeight());
                    int z = getMillimetersDepth(rawDepth, depthX, depthY);
                    float depthZ = z / 1000.0f;

                    Ray ray = arFragment.getArSceneView().getScene().getCamera().screenPointToRay(rightHipX, rightHipY);
                    float[] intersection = computeRayPlaneIntersection(ray, depthZ);

                    if (intersection != null) {
                        float adjustedX = intersection[0] - 0.6096f;

                        com.google.ar.core.Pose landmarkPose = com.google.ar.core.Pose.makeTranslation(intersection[0], intersection[1], intersection[2]);
                        Anchor anchor = session.createAnchor(landmarkPose);
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
                        model.setParent(anchorNode);
                        model.setRenderable(this.model3)
                                .animate(true).start();
                        model.select();

                        // Schedule the removal of the spot after 3 seconds
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            anchorNode.setParent(null);  // This removes the anchor node and associated model
                            Log.d("ARCore", "Spot removed after 3 seconds.");

                            // Call the mainVideoNode function after the spot is destroyed
                            mainVideoNode(frame, session);
                        }, 10000);
                    } else {
                        Log.e("DepthImage", "Depth image acquisition failed.");
                    }

                } catch (Exception e) {
                    Log.e("AnchorCreation", "Exception during anchor creation", e);
                }
            } else {
                Log.e("pose landmarks", "One or more landmarks are null");
            }
        } else {
            Log.d("pose array", "is empty");
        }
    }
    private void heartAttackFunction() {
        startTime = System.currentTimeMillis();
        Log.e("HEART ATTACK FUNCTION", "HEART ATTACK CALLED");

        // Get the AR session and frame
        Session session = arFragment.getArSceneView().getSession();
        Frame frame = arFragment.getArSceneView().getArFrame();

        // Check if the frame is valid
        if (frame == null) {
            Log.e("FrameUpdate", "Frame is null.");
            return;
        }

        // Ensure ARCore is tracking
        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            Log.e("ARCore", "ARCore is not tracking.");
            return;
        }
        String query = "Point out any dangerous objects that are near the patient, point out any pillows or places to move the patient to a more comfortable sitting down position";
        // Additional ARCore setup if needed
        GeminiPro.getResponse(chatModel, query, latestBitmap, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response.contains("e")){
                    try {
                        resumeARCore();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    heartAttackFunction();
                }
                populateChatBody("Gemini", response);
                tts.speak(response, TextToSpeech.QUEUE_ADD, null);
            }


            @Override
            public void onError(Throwable throwable) {
                populateChatBody("Gemini", "Sorry, I'm having trouble understanding that. Please try again.");
            }
        });
//        //best settings for cpr guy
//        Anchor newMarkAnchor = session.createAnchor(
//                frame.getCamera().getPose()
//                        .compose(com.google.ar.core.Pose.makeTranslation(0, -0.2f, -1.4f))
//                        .extractTranslation());
//        AnchorNode anchorNode = new AnchorNode(newMarkAnchor);
//        anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//        TransformableNode model = new TransformableNode(arFragment.getTransformationSystem());
//        model.setParent(anchorNode);
//        model.setRenderable(this.model)
//                .animate(true).start();
//        model.select();


        //placeSpot(frame,session);

        // Delay the start of mainVideoNode by 5 seconds (5000 milliseconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mainVideoNode(frame, session);
        }, 30000);
    }


    // Base pose detector with streaming frames, when depending on the pose-detection sdk
    PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .setPreferredHardwareConfigs(CPU_GPU)
                    .build();
    PoseDetector poseDetector = PoseDetection.getClient(options);

    private void setupOverlay() {
        // Initialize Bitmap with the same size as the PreviewView
        previewView.post(() -> {
            int width = previewView.getWidth();
            int height = previewView.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            // Set Bitmap to an ImageView or similar view to display on top of the PreviewView
            ImageView overlayImageView = findViewById(R.id.overlayImageView);
            overlayImageView.setImageBitmap(bitmap);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Clear existing anchor
        if (currentAnchor != null) {
            currentAnchor.detach();
            currentAnchor = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        ttsTextView = findViewById(R.id.ttsTextView);
        chatBodyContainer = findViewById(R.id.chatResponseLayout);
        chatModel = getChatModel();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        if (isTaskRoot()) {
            Intent intent = new Intent(this, LandingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
            }
        });


        arCoreSwitch = findViewById(R.id.arcore_switch);
        arCoreSwitch.setChecked(false);
        arCoreSwitch.setOnCheckedChangeListener(
                (view, checked) -> {
                    Log.i(TAG, "Switching to " + (checked ? "AR" : "non-AR") + " mode.");
                    if (checked) {
                        poseArrayListNew = poseArrayList;
                        arMode = true;

                        try {
                            resumeARCore();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        poseArrayListNew = null;
                        arMode = false;
                        pauseARCore();
                        resumeCamera2();
                    }
                });
        loadModels();
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        micButton = findViewById(R.id.micButton);

        FloatingActionButton fabToggleDrawer = findViewById(R.id.fab_toggle_drawer);
        fabToggleDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);

                }
            }
        });
        micButton.setOnClickListener(v -> {
            if (isListening) {

                stopListening();
            } else {
                arMode = false;
                // Pause ARCore.
                pauseARCore();
                resumeCamera2();

                startListening();
            }
        });

        // Request permissions if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}


            @Override
            public void onBeginningOfSpeech() {


            }

            @Override
            public void onRmsChanged(float rmsdB) {}


            @Override
            public void onBufferReceived(byte[] buffer) {}


            @Override
            public void onEndOfSpeech() {

                isListening = false;
                arMode = true;
                try {
                    resumeARCore();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                micButton.setImageResource(R.drawable.mic_green);

            }


            @Override
            public void onError(int error) {
                isListening = false;
                micButton.setImageResource(R.drawable.mic_green);
                ttsTextView.setText("Error: " + error);
            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    ttsTextView.setText(matches.get(0));
                    userQueryFromSpeech = matches.get(0);
                    String query = userQueryFromSpeech;
                    //userQuery.setText("");
                    populateChatBody("You", query);


                    GeminiPro.getResponse(chatModel, query, latestBitmap, new ResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            if (response.contains("e")){
                                try {
                                    resumeARCore();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                heartAttackFunction();
                            }
                            populateChatBody("Gemini", response);
                            tts.speak(response, TextToSpeech.QUEUE_ADD, null);
                        }


                        @Override
                        public void onError(Throwable throwable) {
                            populateChatBody("Gemini", "Sorry, I'm having trouble understanding that. Please try again.");
                        }
                    });
                } else {
                    ttsTextView.setText("No speech recognized");
                }

                isListening = false;
                micButton.setImageResource(R.drawable.mic_green);
            }


            @Override
            public void onPartialResults(Bundle partialResults) {}


            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
        if (isTaskRoot()) {
            Intent intent = new Intent(this, LandingActivity.class);
            startActivity(intent);
            finish();
            return;}


    }

    private void pauseARCore() {
        if (arFragment != null) {
            arFragment.getArSceneView().pause(); // Pause ARCore session
        }
        // Additional ARCore cleanup if needed
    }
    private void startListening() {
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.startListening(speechRecognizerIntent);
        isListening = true;
        micButton.setImageResource(R.drawable.mic_red); // Change mic icon to indicate listening
    }


    private void stopListening() {

        speechRecognizer.stopListening();
        isListening = false;
        micButton.setImageResource(R.drawable.mic_green); // Change mic icon back
    }


    private void resumeCamera2() {

        findViewById(R.id.camera_preview).setVisibility(View.VISIBLE);
        findViewById(R.id.arFragment).setVisibility(View.GONE);
        startCamera();  // Use CameraX's startCamera method
    }
    private void startCamera() {
        previewView = findViewById(R.id.camera_preview);
        setupOverlay();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void bindCameraX(ProcessCameraProvider cameraProvider) {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll();

        // Camera Selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image Analysis
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1920, 1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            // Perform pose detection or other processing on the image
            processImage(imageProxy);
            //imageProxy.close(); // Close the image once you are done processing
        });

        // Bind to lifecycle
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImage(ImageProxy imageProxy) {
        // Get an InputImage from the imageProxy
        Image mediaImage = imageProxy.getImage();
        // Check if the image is null
        if (mediaImage == null) {
            imageProxy.close(); // Close the imageProxy if the image is null
            return;
        }
        latestBitmap = imageToBitmap(mediaImage);
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());


        // Process the image
        poseDetector.process(image)
                .addOnSuccessListener(
                        pose -> {
                            // Handle the pose object here
                            poseArrayList.add(pose);

                            // Clear previous drawings
                            if (canvas != null) {
                                canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            }

                            // Drawing on Canvas
                            if (bitmap != null && canvas != null) {
                                Paint paint = new Paint();
                                paint.setColor(android.graphics.Color.RED);
                                paint.setStyle(Paint.Style.FILL);
                                paint.setStrokeWidth(10);

                                List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                                for (PoseLandmark landmark : landmarks) {
                                    // Convert landmark coordinates to screen coordinates
                                    int x = (int) (landmark.getPosition().x * previewView.getWidth() / image.getWidth());
                                    int y = (int) (landmark.getPosition().y * previewView.getHeight() / image.getHeight());

                                    // Draw a dot on the Canvas
                                    canvas.drawCircle(x, y, 10, paint);
                                }

                                // Trigger a redraw
                                previewView.postInvalidate();
                            }

                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            e.printStackTrace();
                        })
                .addOnCompleteListener(
                        task -> imageProxy.close());  // Close the image when done
    }

    private void resumeARCore() throws Exception {
        findViewById(R.id.camera_preview).setVisibility(View.GONE);
        findViewById(R.id.arFragment).setVisibility(View.VISIBLE);
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (arFragment != null) {
            // Clear existing anchor
            if (currentAnchor != null) {
                currentAnchor.detach();
                currentAnchor = null;
            }
            arFragment.getArSceneView().resume(); // Resume ARCore session
        }

    }


    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        session.configure(config);
    }



    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);

        // Fine adjust the maximum frame rate
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        // Add the update listener
        arSceneView.getScene().addOnUpdateListener(this::onFrameUpdate);

    }
    private float[] computeRayPlaneIntersection(Ray ray, float planeDepth) {
        // Get ray origin and direction as Vector3
        Vector3 origin = ray.getOrigin();
        Vector3 direction = ray.getDirection();

        // Convert Vector3 to float[] for easier manipulation
        float[] originArray = { origin.x, origin.y, origin.z };
        float[] directionArray = { direction.x, direction.y, direction.z };

        // Check if ray direction is zero to avoid division by zero
        if (directionArray[2] == 0) {
            Log.e("RayIntersection", "Ray direction is zero in the Z axis.");
            return null;
        }

        // Compute the intersection
        float t = (planeDepth - originArray[2]) / directionArray[2]; // Assuming the plane is parallel to the X-Y plane

        // Compute the intersection point
        return new float[]{
                originArray[0] + t * directionArray[0],
                originArray[1] + t * directionArray[1],
                planeDepth
        };
    }
    // This method will be called every frame
    private void onFrameUpdate(FrameTime frameTime) {
        Log.d("FrameUpdate", "A new frame has been rendered");

        // Check if CPR node exists and is not already scheduled for removal
        if (cpr != null && !isCPRScheduledForRemoval) {
            isCPRScheduledForRemoval = true;

            // Schedule the removal of the CPR node after 2 seconds
            handler.postDelayed(() -> {
                if (cpr != null) {
                    cpr.setParent(null); // Remove the CPR node from the scene
                    cpr = null; // Clear the reference
                    Log.d("CPR Removal", "CPR node has been removed from the scene.");
                }
            }, 7000);
        }
    }






    public void loadModels() {
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///android_asset/cpr_big.glb")) // Update this line
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///android_asset/breath_check_model.glb")) // Update this line
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model2 = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///android_asset/arrow_circle.glb")) // Update this line
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model3 = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///android_asset/pulse_dot.glb")) // Update this line
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model4 = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ModelRenderable.builder()
                .setSource(this, Uri.parse("file:///android_asset/chest.glb")) // Update this line
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.model5 = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
        ViewRenderable.builder()
                .setView(this, R.layout.view_model_title)
                .build()
                .thenAccept(viewRenderable -> {
                    MainActivity activity = weakActivity.get();
                    if (activity != null) {
                        activity.viewRenderable = viewRenderable;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        if (model == null || viewRenderable == null) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
            return;
        }



        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
        modelNode.setParent(anchorNode);


        modelNode.setRenderable(this.model3)
                .animate(true).start();
        modelNode.select();
    }


    public Bitmap imageToBitmap(Image image) {
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    public void populateChatBody(String userName, String message) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message_block, null);


        TextView userAgentName = view.findViewById(R.id.userAgentNameTextView);
        TextView userAgentMessage = view.findViewById(R.id.userAgentMessageTextView);


        userAgentName.setText(userName);
        userAgentMessage.setText (message);


        chatBodyContainer.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
    private ChatFutures getChatModel(){
        GeminiPro model = new GeminiPro();
        GenerativeModelFutures modelFutures = model.getModel();


        return modelFutures.startChat();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        this.mode = item.getItemId();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Quit the handler thread
        handlerThread.quitSafely();
        try {
            handlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (MediaPlayer mediaPlayer : this.mediaPlayers) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        Session session = arFragment.getArSceneView().getSession();
        // Stop the ARCore session if it exists
        if (session != null) {
            session.close();
            session = null;
        }

    }

    /** Obtain the depth in millimeters for depthImage at coordinates (x, y). */
    public int getMillimetersDepth(Image depthImage, int x, int y) {
        // The depth image has a single plane, which stores depth for each
        // pixel as 16-bit unsigned integers.
        Image.Plane plane = depthImage.getPlanes()[0];
        int byteIndex = x * plane.getPixelStride() + y * plane.getRowStride();
        ByteBuffer buffer = plane.getBuffer().order(ByteOrder.nativeOrder());
        return Short.toUnsignedInt(buffer.getShort(byteIndex));
    }


}