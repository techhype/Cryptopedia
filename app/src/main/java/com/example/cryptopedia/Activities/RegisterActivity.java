package com.example.cryptopedia.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cryptopedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    //Object and Variable Declarations and Initializations
    ImageView ImgUserPhoto;
    static int PReqCode =1;
    static int REQUESCODE =1;
    Uri pickedImageUri;

    private EditText userName,userEmail,userPassword,userPassword2;
    private ProgressBar loadingProgress;
    private Button regBtn;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //selecting the views based on their id's
        userName = findViewById(R.id.regName);
        userEmail = findViewById(R.id.regMail);
        userPassword =  findViewById(R.id.regPassword);
        userPassword2 =  findViewById(R.id.regPassword2);
        loadingProgress =  findViewById(R.id.regProgressBar);
        regBtn =  findViewById(R.id.regButton);

        //get an instance of the Firebase Database
        mAuth = FirebaseAuth.getInstance();

        //making the progress bar invisible
        loadingProgress.setVisibility(View.INVISIBLE);

        //Register Button Click Functionality
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //make the Register Button and Progress Bar invisible
                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);

                //get the values inputted by the User in the UI
                final String name =  userName.getText().toString();
                final String email =  userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 =  userPassword2.getText().toString();

                //If user didn't fill any of the input fields or if the password didnt match
                if( name.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()
                    || !password.equals(password2)){
                    //then something goes wrong
                    //we need to show the Error Message
                    showMessage("Please Verify all the fields");
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else{
                    //all fields were filled correctly and passwords matched
                    //now we should create new account in Firebase

                    //createAccount method will try to create a account if the email is valid
                    createAccount(name,email,password);

                }

            }
        });

        //selecting the image view based on its id
        ImgUserPhoto =  findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >=22){
                    checkAndRequestForPermission();
                }
                else{
                    OpenGallery();
                }
            }
        });
    }

    //Method to create user account with specific mail and password
    private void createAccount(final String name, String email, String password) {


        //Creates an user account with email and password
        //on Completion calls the OnComplete Listener Method
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //if account created successfully in Firebase
                    if(task.isSuccessful()){

                        //show Toast message that account created
                        showMessage("Hurray!!!Account created Successfully!!");

                        //after we created user account we need to update his Profile pic and Name
                        updateUserInfo( name, pickedImageUri, mAuth.getCurrentUser());
                    }
                    //else account creation process failed
                    else{
                        //show Toast message that account not created and show the Button
                        showMessage("Oh crap!!!! Account creation Failed"+task.getException().getMessage());
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                    }

                }
            });
    }

    //update User Photo and name
    private void updateUserInfo(final String name, Uri pickedImageUri, final FirebaseUser currentUser){

        //Upload user photo into the Firebase Storage and Get Image Url

        //TODO(1) See the Functionality and add comments for this code
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImageUri.getLastPathSegment());
        imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            //Image Uploaded Successfully
            //Now we can get our Image Url
            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    //Url contain user image Url
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(uri)
                            .build();

                    currentUser.updateProfile(profileUpdate)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        //User Info Updated Successfully
                                        showMessage("Easy Peasy!! Registration Completed");
                                        updateUI();
                                    }
                                }
                            });
                }
            });
            }
        });
    }

    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(),Home.class);
        startActivity(homeActivity);
        finish();
    }


    //Method to show Toast Message
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    private void OpenGallery() {

        Intent galleryIntent =  new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(RegisterActivity.this, "Please accept for the requested permission"
                        , Toast.LENGTH_LONG).show();

            }
            else{
                ActivityCompat.requestPermissions(RegisterActivity.this,
                                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    PReqCode);
            }
        }
        else{
            OpenGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode ==RESULT_OK && requestCode==REQUESCODE && data!=null){
            //The user has successfully picked an image
            //We need to save its reference to a Uri variable
            pickedImageUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImageUri);
        }

    }


}

