package com.example.babyone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.babyone.databinding.ActivityMainLandingBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainLanding extends AppCompatActivity {

    private ActivityMainLandingBinding binding;
    private TextView txtvProfileName;
    private TextView txtvProfileDesc;
    private String email;
    String babyTimestamp;
    Period period;
    String gender,age;
    String sourceFragment;

    @Override
    public void onBackPressed() {
        if (sourceFragment.equals("doctor") || sourceFragment.equals("midwife")) {
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.strvalLogoutExit))
                    .setMessage(getString(R.string.strvalLogoutExitTxt))
                    .setPositiveButton(getString(R.string.strvalLogoutYes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Exit the application
                            finishAffinity();
                        }
                    })
                    .setNegativeButton(getString(R.string.strvalLogoutNo), null)
                    .show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // transparent status bar
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        super.onCreate(savedInstanceState);
        binding = ActivityMainLandingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();
        sourceFragment = extras != null ? extras.getString("sourceFragment") : "";

        //Create Fragments
        homeFragment homeView ;
        Fragment profileView;
        Fragment extrasView;

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(binding.getRoot().getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (sourceFragment.equals("doctor")) {
            extrasView = new MedicineFragment();
            email = extras.getString("email");
            NotificationService.sendNotification(this,"Wellcome Again Doctor, Lets track tour patients","Login");
        } else if (sourceFragment.equals("midwife")) {
            extrasView = new VaccineFragment();
            email = extras.getString("email");
            NotificationService.sendNotification(this,"Wellcome Again Midwife, Lets track tour patients","Login");
        } else {
            extrasView = new extrasFragment(); // Default fragment

            //Fetch baby deatils
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                email = firebaseUser.getEmail();
            }
            NotificationService.sendNotification(this,"Wellcome Again, Lets Track Your baby","Login");
        }

        //Binding elements
        txtvProfileName = binding.txtvProfileName;
        txtvProfileDesc = binding.txtvProfileDesc;

        profileView = new profileFragment();

        // Create db instance to fetch data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String collectionName = "guardians/";
        //Overwrite onDataLoaded method
        FirestoreHelper.readFromCollection(db, collectionName, email, new FirestoreHelper.FirestoreDataCallback() {
            @Override
            public void onDataLoaded(HashMap<String, Map<String, Object>> dataMap) {
                // Handle the retrieved data here
                for (Map.Entry<String, Map<String, Object>> entry : dataMap.entrySet()) {
                    Map<String, Object> data = entry.getValue();
                    for (Map.Entry<String, Object> fieldEntry : data.entrySet()) {
                        String fieldName = fieldEntry.getKey();
                        Object fieldValue = fieldEntry.getValue();
                        if (fieldName.equals("babyname")){
                            txtvProfileName.setText(fieldValue.toString());
                        }
                        if (fieldName.equals("baby_gender")){
                            //txtvProfileDesc.setText(fieldValue.toString()+ ", 2 year old");
                            gender = fieldValue.toString();
                        }
                        if (fieldName.equals("baby_bday")) {
                            babyTimestamp = fieldValue.toString();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate babyBirthday = LocalDate.parse(babyTimestamp, formatter);
                            LocalDate currentDate = LocalDate.now();
                            period = Period.between(babyBirthday, currentDate);
                            int years = period.getYears();
                            int months = period.getMonths();
                            age = years+" years " + months+" month old";
                            //txtvProfileDesc.setText(years+"Y " + months+"M");
                        }
                    }
                }
                if (age != null && gender != null) {
                    txtvProfileDesc.setText(gender + ", " + age);
                }
            }
        });

        homeView = new homeFragment();
        homeView.setEmail(email);
//        homeView.setWeight(50);
//        homeView.setHeight(50);
        replaceFragment(homeView);


        binding.bottomNavigationViewML.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(homeView);
                    break;

                case R.id.profile:
                    replaceFragment(profileView);
                    break;

                case R.id.extras:
                    replaceFragment(extrasView);
                    break;
            }

            return true;
        });

        binding.btnLogOut.setOnClickListener((v -> {
            // Sign out from Google
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                // Check condition
                if (task.isSuccessful()) {
                    // When task is successful, sign out from Firebase
                    firebaseAuth.signOut();
                    // Clear cache
                    clearCache();
                    // Display Toast
                    Toast.makeText(binding.getRoot().getContext(), "Logout successful", Toast.LENGTH_SHORT).show();
                    // Finish activity
                    finish();
                    startActivity(new Intent(binding.getRoot().getContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
        }));
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Add a fade-in animation
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, 0, 0, 0);
        fragmentTransaction.replace(binding.frameLayoutML.getId(), fragment);
        fragmentTransaction.commit();
    }

    // Clear cache
    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Delete directory recursively
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}