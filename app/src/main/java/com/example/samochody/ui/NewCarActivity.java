package com.example.samochody.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.samochody.R;
import com.example.samochody.model.db.CarEntity;
import com.example.samochody.model.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// aktywnosc tworzenia nowego auta - podobnie jak w EditCarActivity
public class NewCarActivity extends AppCompatActivity {
    private static final String TAG = NewCarActivity.class.getSimpleName();
    private static final int ACTION_REQUEST_PHOTO = 111;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();

    @BindView(R.id.etCarBrand)
    EditText etCarBrand;
    @BindView(R.id.etCarModel)
    EditText etCarModel;
    @BindView(R.id.etCarPower)
    EditText etCarPower;
    @BindView(R.id.etCarWeight)
    EditText etCarWeight;
    @BindView(R.id.spCarProductionYear)
    Spinner spCarProductionYear;
    @BindView(R.id.spCarType)
    Spinner spCarType;
    @BindView(R.id.spCarEngineType)
    Spinner spCarEngineType;
    @BindView(R.id.spCarDoorsCount)
    Spinner spCarDoorsCount;
    @BindView(R.id.gCarViews)
    Group gCarViews;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.ivCarPhoto)
    ImageView ivCarPhoto;
    private Uri selectedImageUri = null;
    private Uri downloadImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_car);
        ButterKnife.bind(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, Utils.getProductionYears());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarProductionYear.setAdapter(adapter);
    }

    @OnClick(R.id.ivCarPhoto)
    public void onPhotoClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        Intent chooser = Intent.createChooser(intent, "Wybierz obraz");
        startActivityForResult(chooser, ACTION_REQUEST_PHOTO);
    }

    private void uploadImage() {
        StorageReference imagesRef = storageRef.child("images/" + Utils.randId() + ".jpg");
        UploadTask uploadTask = imagesRef.putFile(selectedImageUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            } else
                return imagesRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                downloadImageUri = task.getResult();
                storeCarEntity();
            } else {
                Toast.makeText(NewCarActivity.this, "Przesylanie obrazu nie powiodło się!", Toast.LENGTH_LONG).show();
                pbLoading.setVisibility(View.GONE);
                gCarViews.setEnabled(true);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ACTION_REQUEST_PHOTO) {
            selectedImageUri = data.getData();
            ivCarPhoto.setImageURI(selectedImageUri);
        }
    }

    @OnClick(R.id.bCarSave)
    public void onSaveClick() {
        String brand = etCarBrand.getText().toString();
        String model = etCarModel.getText().toString();
        String powerStr = etCarPower.getText().toString();
        String weightStr = etCarWeight.getText().toString();

        if(brand.isEmpty() || model.isEmpty() || powerStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_LONG).show();
            return;
        }

        gCarViews.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);
        if(selectedImageUri != null)
            uploadImage();
        else
            storeCarEntity();
    }

    private void storeCarEntity() {
        String brand = etCarBrand.getText().toString();
        String model = etCarModel.getText().toString();
        String powerStr = etCarPower.getText().toString();
        String weightStr = etCarWeight.getText().toString();

        double power = Double.parseDouble(powerStr);
        double weight = Double.parseDouble(weightStr);
        int productionYear = Integer.parseInt((String) spCarProductionYear.getSelectedItem());
        int doorsCount = Integer.parseInt((String) spCarDoorsCount.getSelectedItem());
        String downloadUrl = downloadImageUri != null ? downloadImageUri.toString() : "";

        String id = Utils.randId();
        CarEntity carEntity = new CarEntity(
                id,
                brand,
                model,
                downloadUrl,
                productionYear,
                doorsCount,
                spCarType.getSelectedItemPosition(),
                spCarEngineType.getSelectedItemPosition(),
                power,
                weight
        );

        db.collection(Utils.CARS_COLLECTION)
                .document(id)
                .set(carEntity)
                .addOnSuccessListener(o -> {
                    Log.d(TAG, "Dokument zapisany");
                    Toast.makeText(NewCarActivity.this, "Samochód został zapisany.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Blad podczas dodawania dokumentu", e);
                    Toast.makeText(NewCarActivity.this, "Wystąpił błąd podczas dodawania dokumentu", Toast.LENGTH_LONG).show();
                    pbLoading.setVisibility(View.GONE);
                    gCarViews.setEnabled(true);
                });

    }
}