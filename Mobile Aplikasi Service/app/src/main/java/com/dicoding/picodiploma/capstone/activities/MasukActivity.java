package com.dicoding.picodiploma.capstone.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.dicoding.picodiploma.capstone.databinding.ActivityMasukBinding;
import com.dicoding.picodiploma.capstone.utilities.Constants;
import com.dicoding.picodiploma.capstone.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;


public class MasukActivity extends AppCompatActivity {

    private ActivityMasukBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivityMasukBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void  setListeners(){
        binding.textAkunBaru.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), DaftarActivity.class)));
        binding.tombolMasuk.setOnClickListener(v -> {
            if (isValidMasukDetails()){
                masuk();
            }
        });
    }

    private void masuk(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.Email.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString((Constants.KEY_IMAGE)));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("Login Gagal");
                    }
                });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.tombolMasuk.setVisibility(View.INVISIBLE);
            binding.barProgress.setVisibility(View.VISIBLE);
        }else{
            binding.barProgress.setVisibility(View.INVISIBLE);
            binding.tombolMasuk.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidMasukDetails(){
        if(binding.Email.getText().toString().trim().isEmpty()) {
            showToast("Masukan Email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.Email.getText().toString()).matches()) {
            showToast("Masukan Email yang Benar");
            return false;
        }else if(binding.inputPassword.getText().toString().isEmpty()){
            showToast("Masukan Password");
            return false;
        }else {
            return true;
        }
    }
}