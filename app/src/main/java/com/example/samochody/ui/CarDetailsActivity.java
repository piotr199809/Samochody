package com.example.samochody.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.samochody.R;
import com.example.samochody.model.db.CarEntity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CarDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_CAR_ITEM = "EXTRA_CAR_ITEM";
    private CarEntity carEntity;        // obiekt samochodu, ktorego szczegoly maja byc wyswietlone
    private String[] carTypes, engineTypes;     // lista naz typow nadwozia i silnika
    @BindView(R.id.ivCarDetailsPhoto)
    ImageView ivCarDetailsPhoto;
    @BindView(R.id.tvCarDetailsName)
    TextView tvCarDetailsName;
    @BindView(R.id.tvCarDetailsEngine)
    TextView tvCarDetailsEngine;
    @BindView(R.id.tvCarDetailsEnginePower)
    TextView tvCarDetailsEnginePower;
    @BindView(R.id.tvCarDetailsDoorsCount)
    TextView tvCarDetailsDoorsCount;
    @BindView(R.id.tvCarDetailsWeight)
    TextView tvCarDetailsWeight;
    @BindView(R.id.tvCarDetailsProductionYear)
    TextView tvCarDetailsProductionYear;
    @BindView(R.id.tvCarDetailsType)
    TextView tvCarDetailsType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);
        ButterKnife.bind(this);

        carEntity = getIntent().getParcelableExtra(EXTRA_CAR_ITEM);     // pobranie obiektu samochodu przekazanego przy tworzeniu intencji
        // lista typow silnika i nadwozia
        carTypes = getResources().getStringArray(R.array.car_type);
        engineTypes = getResources().getStringArray(R.array.engine_type);

        fillData();     // wypelnienie widokow danymi
    }

    @SuppressLint("SetTextI18n")
    private void fillData() {
        // jesli obraz istnieje
        if(carEntity.getImage() != null && !carEntity.getImage().isEmpty())
            Glide.with(this)
                .load(carEntity.getImage())
                .into(ivCarDetailsPhoto);

        String name = carEntity.getBrand() + " " + carEntity.getModel();
        tvCarDetailsName.setText(name);
        tvCarDetailsDoorsCount.setText(Integer.toString(carEntity.getDoorsCount()));
        tvCarDetailsProductionYear.setText(Integer.toString(carEntity.getProductionYear()));
        tvCarDetailsEnginePower.setText(Double.toString(carEntity.getEnginePower()));
        tvCarDetailsWeight.setText(Double.toString(carEntity.getWeight()));

        // w obiekcie zapisane sa indexy wartosci silnika i typu nadwozia, a nie realne dane, wiec
        tvCarDetailsType.setText(carTypes[carEntity.getCarType()]);
        tvCarDetailsEngine.setText(engineTypes[carEntity.getEngineType()]);
    }
}