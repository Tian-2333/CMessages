package com.example.cmessages.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmessages.databinding.ActivitySignInBinding;
import com.example.cmessages.utilities.Constants;
import com.example.cmessages.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent  =new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.CreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonLoginIn.setOnClickListener(v-> {
            if(isValidSignInDetails()){
                login();
            }
        });

    }

    private void login() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task-> {
                    if(task.isSuccessful() && task.getResult() != null &&
                    task.getResult().getDocuments().size() >0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent;
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }else{
                        loading(false);
                        showToast("Login failed");
                    }

                });

    }


    private void loading(Boolean isLoading){
        if(isLoading) {
            binding.buttonLoginIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLoginIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please enter your email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Please enter a vaild email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please enter your password");
            return false;
        }else{
            return true;
        }
    }
}