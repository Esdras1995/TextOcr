package com.rapidocr.root.textocr;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.scanlibrary.IScanner;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


//http://asprise.com/document-scanning-library/java-image-scanner-read-text-from-images-resources.html#toc-title-0
public class TextRecognition extends ActionBarActivity implements IScanner{

    public static final String PACKAGE_NAME = "com.example.root.tesstwo";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/TextRecognition/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // http://code.google.com/p/tesseract-ocr/downloads/list
    public static final String lang = "fra";

    private static final String TAG = "TextRecognition.java";

    private ImageButton _camera;
    private ImageButton _image;
    private Toolbar toolbar;

    // protected ImageView _image;
    protected EditText _field;
    protected String _path;
    protected boolean _taken;

    protected static final String PHOTO_TAKEN = "photo_taken";
    private static final int CAMERA_REQUEST = 0;
    private static final int SELECT_IMAGE_REQUEST = 1;
    private String selectedImagePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            Log.v(TAG, "Je suis ici !!!");
            InputStream in = null;
            try {

                AssetManager assetManager = getAssets();
                in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        // _image = (ImageView) findViewById(R.id.image);
        _field = (EditText) findViewById(R.id.field);
        _camera = (ImageButton) findViewById(R.id.btnCamera);
        _image = (ImageButton) findViewById(R.id.btnSelectImage);
        _camera.setOnClickListener(new ButtonClickHandler());
        _image.setOnClickListener(new ButtonSelectImage());

        _path = DATA_PATH + "/ocr.jpg";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBitmapSelect(Uri uri) {

    }

    @Override
    public void onScanFinish(Uri uri) {

    }

    public class ButtonClickHandler implements View.OnClickListener {
        public void onClick(View view) {
            Log.v(TAG, "Starting Camera app");
            startCameraActivity();
        }
    }

    public class ButtonSelectImage implements View.OnClickListener{
        public int preference;

        public ButtonSelectImage(int preference){
            this.preference = preference;
        }

        public ButtonSelectImage(){}

        @Override
        public void onClick(View view) {
            Log.v(TAG, "Select button active");
            startSelectImageActivity(preference);
        }
    }

    protected void startSelectImageActivity(int preference) {
        /* Avant */
//        // 1. on Upload click call ACTION_GET_CONTENT intent
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        // 2. pick image only
//        intent.setType("image/*");
//        // 3. start activity
//        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_IMAGE_REQUEST);

        /* Apres */

        startScan(preference);
        // define onActivityResult to do something with picked image
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST);
    }
    // Simple android photo capture:
    // http://labs.makemachine.net/2010/03/simple-android-photo-capture/

    protected void startCameraActivity() {
        File file = new File(_path);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + RESULT_OK + "result for requestCode: [ " + requestCode + " ]");

        if (requestCode == CAMERA_REQUEST){
            if (resultCode == RESULT_OK) {
                onPhotoTaken();
            } else {
                Log.v(TAG, "User cancelled");
            }
        }else if( requestCode == SELECT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            //onPathImageTaken();
//            if (data == null) return;
//
//            String selectedImagePath;
//            Uri selectedImageUri = data.getData();
//
//            //MEDIA GALLERY
//            selectedImagePath = ImageFilePath.getPath(getApplicationContext(), selectedImageUri);
//            Log.v(TAG, ""+selectedImagePath);
//            _path = selectedImagePath;
            onPhotoTaken(data);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TextRecognition.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(TextRecognition.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }
    protected void onPhotoTaken() {}
    protected void onPhotoTaken(Intent data) {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            getContentResolver().delete(uri, null, null);
//            scannedImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);
//        try {
//            ExifInterface exif = new ExifInterface(_path);
//            int exifOrientation = exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL);
//
//            Log.v(TAG, "Orient: " + exifOrientation);
//
//            int rotate = 0;
//
//            switch (exifOrientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    rotate = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    rotate = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    rotate = 270;
//                    break;
//            }
//
//            Log.v(TAG, "Rotation: " + rotate);
//
//            if (rotate != 0) {
//
//                // Getting width & height of the given image.
//                int w = bitmap.getWidth();
//                int h = bitmap.getHeight();
//
//                // Setting pre rotate
//                Matrix mtx = new Matrix();
//                mtx.preRotate(rotate);
//
//                // Rotating Bitmap
//                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
//            }
//
//            // Convert to ARGB_8888, required by tess
//            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//
//        } catch (IOException e) {
//            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
//        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);

        Log.v(TAG, "Ici after debug!!");
        baseApi.init(DATA_PATH, lang);
        Log.v(TAG, "Ici after init!!");
        baseApi.setImage(bitmap);
        Log.v(TAG, "Ici after setimage");

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if ( recognizedText.length() != 0 ) {
            _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
            _field.setSelection(_field.getText().toString().length());
        }

        // Cycle done.
    }

    // www.Gaut.am was here
    // Thanks for reading!
}
