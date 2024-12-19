package com.ghost_byte.fy_foundyoutheparentcontroll;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailsWIndow extends AppCompatActivity {
    private TextView nameitem, timeItem, btnReply;
    private ImageView delViewBtn, backBtn;
    private LinearLayout delMenu, delInfo ;

    private String lastlatitude;
    private String lastlongtitude;

    private boolean isMenuOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        delViewBtn = findViewById(R.id.delSHowBTN);
        delMenu = findViewById(R.id.delMenu);
        btnReply = findViewById(R.id.replyBtn);
        backBtn = findViewById(R.id.backArrow);
        delInfo = findViewById(R.id.deletInfo);


        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String id = intent.getStringExtra("uniqueCode");

        nameitem = findViewById(R.id.nameRecycleItem);
        nameitem.setText(name);
        getTime(id);

        delInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize Firestore instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Initialize local database helper
                DataBaseHelper dataBaseHelper = new DataBaseHelper(DetailsWIndow.this);

                // Delete from Firestore using the id as the document name
                db.collection("locationInfo").document(id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Delete from local SQLite database after successful Firestore deletion
                                boolean isDeleted = dataBaseHelper.deleteData(id);

                                if (isDeleted) {
                                    // Show success message and navigate to MainActivity
                                    Toast.makeText(DetailsWIndow.this, "Record deleted successfully !", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(DetailsWIndow.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Firestore success but local SQLite deletion failed
                                    Toast.makeText(DetailsWIndow.this, "Record delete failed. ", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Firestore deletion failed
                                Toast.makeText(DetailsWIndow.this, "Failed to delete failed.", Toast.LENGTH_LONG).show();

                                // Attempt local deletion even if Firestore deletion fails
                                boolean isDeleted = dataBaseHelper.deleteData(id);

                                if (isDeleted) {
                                    Toast.makeText(DetailsWIndow.this, "Record deleted failed.", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(DetailsWIndow.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Both Firestore and local deletion failed
                                    Toast.makeText(DetailsWIndow.this, "Failed to delete record.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


        //backpress event controll
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(DetailsWIndow.this, MainActivity.class));
                finish();
                Log.d("Back","Back pressed");
            }
        });

        delViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMenuOpened == false){
                    showMenu(delMenu);
                    isMenuOpened = true;

                    return;
                }

                if (isMenuOpened == true){
                    hideMenu(delMenu);
                    isMenuOpened = false;

                    return;
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DetailsWIndow.this, MainActivity.class));
                finish();
            }
        });

        btnReply.setOnClickListener(v -> {
            if (lastlatitude == null || lastlatitude.isEmpty() || lastlongtitude == null || lastlongtitude.isEmpty()) {
                Log.w("location", "Latitude or longitude not found");
                return;
            }

            try {
                // Construct query-only URI
                Uri googleMapsUri = Uri.parse("geo:" + lastlatitude + "," + lastlongtitude + "?q=" + lastlatitude + "," + lastlongtitude + "(Target Location)&z=20");
                Log.d("GeoURI", "Constructed URI: " + googleMapsUri.toString());

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapsUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Ensure the Google Maps app is used

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Log.w("Maps", "No map app found, opening in browser");
                    Uri browserUri = Uri.parse("https://www.google.com/maps?q=" + lastlatitude + "," + lastlongtitude);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                    startActivity(browserIntent);
                }
            } catch (Exception e) {
                Log.e("Maps", "Error launching Google Maps", e);
                Toast.makeText(DetailsWIndow.this, "Error launching Google Maps", Toast.LENGTH_LONG).show();
            }
        });





    }



    public void showMenu(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(delMenu,"translationX", 500f,0f);
        animator.setDuration(300);
        animator.start();
    }
    public void hideMenu(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(delMenu,"translationX", 0f,500f);
        animator.setDuration(300);
        animator.start();
    }

    public void getTime(String id){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        timeItem = findViewById(R.id.timeRecycleItem);

        db.collection("locationInfo")
                .document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()){
                            String time = documentSnapshot.getString("time");
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longtitude = documentSnapshot.getDouble("longitude");
                            timeItem.setText(time);
                            lastlongtitude = String.valueOf(longtitude);
                            lastlatitude = String.valueOf(latitude);
                        }else {
                            Log.d("firbase", "No data found");
                        }
                    }else {
                        Log.w("firebase", "Error", task.getException());
                    }
                });

    }


}