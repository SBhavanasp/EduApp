package com.example.eduapp.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.eduapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.type.DateTime;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnswerActivity extends AppCompatActivity{

    static final int PICK_CAPTURE = 1;
    ImageView imageView;
    Button button;
    FloatingActionButton fab;
    StorageReference fs;
    FirebaseFirestore ff;
    FirebaseAuth fa;
    TextInputEditText desc;
//    Spinner spinner;
    String postid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        fs = FirebaseStorage.getInstance().getReference().child("Images");
        fa = FirebaseAuth.getInstance();
        postid = getIntent().getStringExtra("postid");
//        title = findViewById(R.id.post_title);
        desc = findViewById(R.id.answer_post_desc);
        Toolbar toolbar = findViewById(R.id.answer_post_tool_bar);
        setSupportActionBar(toolbar);
        ff = FirebaseFirestore.getInstance();
        button = findViewById(R.id.answer_post_image_picker);
        imageView = findViewById(R.id.answer_post_image_picked);

//        spinner = findViewById(R.id.post_select);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.classes,android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(this);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        fab = findViewById(R.id.answer_post_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the data from an ImageView as bytes
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                final StorageReference sr = fs.child(UUID.randomUUID().toString());
                Toast.makeText(AnswerActivity.this,"Upload Started",Toast.LENGTH_SHORT).show();
                sr.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return sr.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            if(desc.getText().toString().isEmpty()){
                                desc.setError("Required Field");
                                desc.requestFocus();
                            }else {
                                final Map<String,Object> map = new HashMap<>();
                                map.put("description",desc.getText().toString());
                                map.put("imageurl",downloadUri.toString());
                                map.put("userid",fa.getUid());
                                DocumentReference docref = ff.collection("Users").document(fa.getUid());
                                docref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                            if (doc.get("name") != null) {
                                                map.put("name",doc.get("name"));
                                            }
                                            if (doc.get("class") != null) {
                                                map.put("class",doc.get("class"));
                                            }
                                            map.put("time", System.currentTimeMillis());
                                            ff.collection("Posts").document(postid).collection("answers").add(map);
                                            Toast.makeText(AnswerActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(AnswerActivity.this,"Upload Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ;

            }
        });
    }

    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CAPTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    /*@Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        class_name = (String)adapterView.getItemAtPosition(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }*/
}