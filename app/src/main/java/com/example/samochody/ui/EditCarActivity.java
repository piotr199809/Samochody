package com.example.samochody.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.bumptech.glide.Glide;
import com.example.samochody.R;
import com.example.samochody.model.Utils;
import com.example.samochody.model.db.CarEntity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditCarActivity extends AppCompatActivity {
    public static final String EXTRA_CAR_ITEM = "EXTRA_CAR_ITEM";
    private static final String TAG = EditCarActivity.class.getSimpleName();
    private static final int ACTION_REQUEST_PHOTO = 111;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();
    private CarEntity carEntity;
    private List<String> doorsCountList;
    private List<String> productionYears;

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
    @BindView(R.id.bCarSave)
    Button bCarSave;
    private Uri selectedImageUri = null;    // uri do wybranego obrazu
    private Uri downloadImageUri = null;    // uri do obrazu zapisanego w firebase storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_car);
        ButterKnife.bind(this);

        productionYears = Utils.getProductionYears();   // obliczenie lat produkcji od 1920 do teraz
        // stworzenie adaptera i zapisanie go
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, productionYears);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarProductionYear.setAdapter(adapter);
        bCarSave.setText(R.string.save);

        carEntity = getIntent().getParcelableExtra(EXTRA_CAR_ITEM);
        doorsCountList = Arrays.asList(getResources().getStringArray(R.array.doors_count));

        fillData();
    }

    // wypelnienie pol danymi z przekazanego obiektu
    @SuppressLint("SetTextI18n")
    private void fillData() {
        if(carEntity.getImage() != null && !carEntity.getImage().isEmpty())
            Glide.with(this)
                    .load(carEntity.getImage())
                    .into(ivCarPhoto);

        String doorsCount = Integer.toString(carEntity.getDoorsCount());
        String productionYear = Integer.toString(carEntity.getProductionYear());
        etCarBrand.setText(carEntity.getBrand());
        etCarModel.setText(carEntity.getModel());
        spCarDoorsCount.setSelection(doorsCountList.indexOf(doorsCount));
        spCarProductionYear.setSelection(productionYears.indexOf(productionYear));
        etCarPower.setText(Double.toString(carEntity.getEnginePower()));
        etCarWeight.setText(Double.toString(carEntity.getWeight()));

        // w obiekcie zapisane sa indexy wartosci silnika i typu nadwozia, a nie realne dane, wiec
        spCarType.setSelection(carEntity.getCarType());
        spCarEngineType.setSelection(carEntity.getEngineType());
    }

    // wybor obrazu
    @OnClick(R.id.ivCarPhoto)
    public void onPhotoClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        Intent chooser = Intent.createChooser(intent, "Wybierz obraz");
        startActivityForResult(chooser, ACTION_REQUEST_PHOTO);
    }


    // upload obrazu
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
                // pobranie url do obrazu w firebase storage
                downloadImageUri = task.getResult();
                storeCarEntity();   // zapis edycji danych
            } else {
                Toast.makeText(EditCarActivity.this, "Przesylanie obrazu nie powiodło się!", Toast.LENGTH_LONG).show();

                // wylaczenie ladowania i odblokowanie widokow
                pbLoading.setVisibility(View.GONE);
                gCarViews.setEnabled(true);
            }
        });
    }

    // powrot z wyboru obrazu
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ACTION_REQUEST_PHOTO) {
            selectedImageUri = data.getData();  // uri wybranego obrazu
            ivCarPhoto.setImageURI(selectedImageUri);   // pokazanie obrazu
        }
    }

    // zapis danych
    @OnClick(R.id.bCarSave)
    public void onSaveClick() {
        String brand = etCarBrand.getText().toString();
        String model = etCarModel.getText().toString();
        String powerStr = etCarPower.getText().toString();
        String weightStr = etCarWeight.getText().toString();

        // sprawdzenie, czy pola sa wypelnione
        if(brand.isEmpty() || model.isEmpty() || powerStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_LONG).show();
            return;
        }

        // zablokowanie widgetow i pokazanie koleczka ladowania
        gCarViews.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);

        // jesli uzytkownik zmienil obraz
        if(selectedImageUri != null) {
            removeImage();  // usun obecny
            uploadImage();  // upload wybranego
        }
        else
            storeCarEntity();       // tylko zapis danych
    }

    // usuniecie obecnego obrazu samochodu
    private void removeImage() {
        String image = carEntity.getImage();

        if(image != null && !image.isEmpty()) {
            String filename = Utils.retrieveImageFilename(image);
            StorageReference desertRef = storageRef.child("images/" + filename);

            desertRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Obraz usuniety"))
                    .addOnFailureListener(exception -> Log.e(TAG, "Obraz nie został usuniety", exception));
        }
    }

    private void storeCarEntity() {
        // pobranie danych z widokow
        String brand = etCarBrand.getText().toString();
        String model = etCarModel.getText().toString();
        String powerStr = etCarPower.getText().toString();
        String weightStr = etCarWeight.getText().toString();

        double power = Double.parseDouble(powerStr);
        double weight = Double.parseDouble(weightStr);
        int productionYear = Integer.parseInt((String) spCarProductionYear.getSelectedItem());
        int doorsCount = Integer.parseInt((String) spCarDoorsCount.getSelectedItem());
        // jesli zmieniony obraz to podanie nowego, jesli nie, to wprowadzanie tego co byl
        String downloadUrl = downloadImageUri != null ? downloadImageUri.toString() : carEntity.getImage();

        // aktualizacja pol
        carEntity.setBrand(brand);
        carEntity.setModel(model);
        carEntity.setEnginePower(power);
        carEntity.setWeight(weight);
        carEntity.setProductionYear(productionYear);
        carEntity.setDoorsCount(doorsCount);
        carEntity.setCarType(spCarType.getSelectedItemPosition());
        carEntity.setEngineType(spCarEngineType.getSelectedItemPosition());
        carEntity.setImage(downloadUrl);

        // zapis obiektu w firestore
        db.collection(Utils.CARS_COLLECTION)
                .document(carEntity.getId())
                .set(carEntity)
                .addOnSuccessListener(o -> {
                    Log.d(TAG, "Dokument zapisany");
                    Toast.makeText(EditCarActivity.this, "Samochód został zapisany.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Blad podczas dodawania dokumentu", e);
                    Toast.makeText(EditCarActivity.this, "Wystąpił błąd podczas dodawania dokumentu", Toast.LENGTH_LONG).show();
                    pbLoading.setVisibility(View.GONE);
                    gCarViews.setEnabled(true);
                });

    }
}