package gottnext.com.devicecamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gottnext.com.devicecamera.app.AppController;

public class MainActivity extends AppCompatActivity {

    Button btnChoose;
    FloatingActionButton btnUpload;
    Toolbar toolbar;
    ImageView imageView;
    EditText editText;
    Bitmap bitmap, decoded;
    int success, PICK_IMAGE_REQUEST = 1, bitmapSize = 60;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String UPLOAD_URL = "http://gottnext.com/DeviceCamera/image/";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String KEY_NAME = "name";
    private String KEY_IMAGE = "image";
    String tagJsonObject = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        editText = findViewById(R.id.editT);
        imageView = findViewById(R.id.imageV);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    public String getStringImage(Bitmap btmp) {
        byte[] imageBytes;
        String encodeImage;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        btmp.compress(Bitmap.CompressFormat.JPEG, bitmapSize, baos);
        imageBytes = baos.toByteArray();
        encodeImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodeImage;
    }

    private void uploadImage() {
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    if(success == 1) {
                        Log.e("Add", jObj.toString());
                        Toast.makeText(MainActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                        isEmpty();
                    }
                    else {
                        Toast.makeText(MainActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }
                loading.dismiss();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this,error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.getMessage().toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(KEY_NAME, editText.getText().toString().trim());
                params.put(KEY_IMAGE, getStringImage(decoded));
                Log.e(TAG, "" + params);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagJsonObject);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("images/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                setToImageView(getResizedBitmap(bitmap, 512));
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void isEmpty() {
        imageView.setImageResource(0);
        editText.setText(null);
    }

    private void setToImageView(Bitmap btmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        btmp.compress(Bitmap.CompressFormat.JPEG, bitmapSize, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));
        imageView.setImageBitmap(decoded);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float)width / (float)height;
        if(bitmapRatio > 1) {
            width = maxSize;
            width = (int)(height / bitmapRatio);
        }
        else {
            height = maxSize;
            width = (int)(height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
