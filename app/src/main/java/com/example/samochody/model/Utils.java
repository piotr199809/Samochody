package com.example.samochody.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

// klasa pomocnicza
public class Utils {
    public static final String CARS_COLLECTION = "cars";        // nazwa kolekcji w firebase firestore

    // generacja losowego id
    public static String randId() {
        return UUID.randomUUID().toString();
    }

    // wyliczenie lat od 1920 do obecnego
    public static List<String> getProductionYears() {
        List<String> result = new ArrayList<>();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        for(int i = 1920; i <= year; i++)
            result.add(Integer.toString(i));
        return result;
    }

    // uzyskanie nazwy pliku z linku do zdjecia w firebase storage
    public static String retrieveImageFilename(String imageUrl) {
        int indexStart = imageUrl.indexOf("%2F") + 3;
        int indexEnd = imageUrl.indexOf("?alt=");
        return imageUrl.substring(indexStart, indexEnd);
    }
}
