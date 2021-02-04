package com.example.facedetection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.facedetection.Retrofit.IUploadAPI;
import com.example.facedetection.Retrofit.RetrofitClient;
import com.example.facedetection.Utils.Common;
import com.example.facedetection.Utils.IUploadCallBacks;
import com.example.facedetection.Utils.ProgressRequestBody;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements IUploadCallBacks {

    private static final int PICK_FILE_REQUEST = 1000;

    IUploadAPI mService;
    ImageView imageView , final_image_view;
    CardView upload;
    Uri selectedFileUri;

    ProgressDialog dialog ;

    private IUploadAPI getAPIUpload()
    {
        return RetrofitClient.getRetrofitClient().create(IUploadAPI.class);
    }
    public void upload(View view)
    {
        uploadFile();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Granted", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Please accept to continue", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();



        //create mService
        mService = getAPIUpload();
        //INITVALUES
        imageView = (ImageView)findViewById(R.id.image_view);
        final_image_view = (ImageView)findViewById(R.id.final_image_view) ;
        upload = (CardView)findViewById(R.id.login_button_card_view) ;
        upload.setVisibility(View.INVISIBLE);
        upload.setClickable(false);
        final_image_view.setVisibility(View.INVISIBLE);
        final_image_view.setClickable(false);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
                final_image_view.setVisibility(View.VISIBLE);
                final_image_view.setClickable(true);
                upload.setVisibility(View.VISIBLE);
                upload.setClickable(true);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if(requestCode == PICK_FILE_REQUEST)
            {
                if(data != null)
                {
                    selectedFileUri = data.getData();
                    if(selectedFileUri != null && !selectedFileUri.getPath().isEmpty())
                        final_image_view.setImageURI(selectedFileUri);
                    else
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_FILE_REQUEST);
    }
     private void uploadFile()
     {
         if(selectedFileUri !=null) {
             dialog = new ProgressDialog(MainActivity.this);
             dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             dialog.setMessage("Uploading....");
             dialog.setIndeterminate(false);
             dialog.show();
             File file = null;
             try {
                 file = new File(Common.getFilePath(this, selectedFileUri));
             } catch (URISyntaxException e) {
                 e.printStackTrace();
             }

             if (file != null) {

                 final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);

                 MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         mService.uploadFile(body)
                                 .enqueue(new Callback<String>() {
                                     @Override
                                     public void onResponse(Call<String> call, Response<String> response) {
                                         dialog.dismiss();
                                         String image_processed_link = new StringBuilder("http://192.168.0.105:5000/" +
                                                 response.body().replace("\"", "")).toString();
                                         Picasso.get().load(image_processed_link).into(final_image_view);

                                     }

                                     @Override
                                     public void onFailure(Call<String> call, Throwable t) {
                                         dialog.dismiss();
                                         Toast.makeText(MainActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                                     }
                                 });
                     }
                 }).start();
             }
         }else
         {
             Toast.makeText(this, "Cannot Upload file!!", Toast.LENGTH_SHORT).show();
         }
     }

    @Override
    public void OnProgressUpdate(int percent) {

    }
}