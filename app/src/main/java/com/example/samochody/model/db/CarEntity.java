package com.example.samochody.model.db;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

// obiekt klasy samochod
public class CarEntity implements Parcelable {
    private String id;
    private String brand;   // marka
    private String model;   // model
    private String image;   // obraz
    private int productionYear;     // rok produkcji
    private int doorsCount;     // liczba drzwi
    private int carType;        // rodzaj nadwozia (przechowywany jest index do tablicy z strings.xml)
    private int engineType;     // rodzaj silnika (przechowywany jest index do tablicy z strings.xml)
    private double enginePower;     // moc silnika
    private double weight;      // waga

    // pusty konstruktor wymagany do konwersji wynikow z firestore na obiekt
    public CarEntity() { }

    public CarEntity(String id, String brand, String model, String image, int productionYear, int doorsCount, int carType, int engineType, double enginePower, double weight) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.image = image;
        this.productionYear = productionYear;
        this.doorsCount = doorsCount;
        this.carType = carType;
        this.engineType = engineType;
        this.enginePower = enginePower;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public int getDoorsCount() {
        return doorsCount;
    }

    public void setDoorsCount(int doorsCount) {
        this.doorsCount = doorsCount;
    }

    public int getCarType() {
        return carType;
    }

    public void setCarType(int carType) {
        this.carType = carType;
    }

    public int getEngineType() {
        return engineType;
    }

    public void setEngineType(int engineType) {
        this.engineType = engineType;
    }

    public double getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(double enginePower) {
        this.enginePower = enginePower;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarEntity carEntity = (CarEntity) o;
        return Objects.equals(id, carEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // zapis do parcelable w celu mozliwosci przekazania calego obiektu pomiedzy aktywnosciami w argumencie intencji
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.brand);
        dest.writeString(this.model);
        dest.writeString(this.image);
        dest.writeInt(this.productionYear);
        dest.writeInt(this.doorsCount);
        dest.writeInt(this.carType);
        dest.writeInt(this.engineType);
        dest.writeDouble(this.enginePower);
        dest.writeDouble(this.weight);
    }

    protected CarEntity(Parcel in) {
        this.id = in.readString();
        this.brand = in.readString();
        this.model = in.readString();
        this.image = in.readString();
        this.productionYear = in.readInt();
        this.doorsCount = in.readInt();
        this.carType = in.readInt();
        this.engineType = in.readInt();
        this.enginePower = in.readDouble();
        this.weight = in.readDouble();
    }

    public static final Parcelable.Creator<CarEntity> CREATOR = new Parcelable.Creator<CarEntity>() {
        @Override
        public CarEntity createFromParcel(Parcel source) {
            return new CarEntity(source);
        }

        @Override
        public CarEntity[] newArray(int size) {
            return new CarEntity[size];
        }
    };
}
