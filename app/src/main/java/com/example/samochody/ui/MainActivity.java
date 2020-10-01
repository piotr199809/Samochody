package com.example.samochody.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.samochody.R;
import com.example.samochody.model.Utils;
import com.example.samochody.model.db.CarEntity;
import com.example.samochody.ui.adapter.CarItemListener;
import com.example.samochody.ui.adapter.CarsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


// glowna aktywnosc
public class MainActivity extends AppCompatActivity implements CarItemListener {
    private static final int PERMISSION_REQUEST_CODE = 111;     // request code dla prosby o pozwolenie do dostepu do plikow
    private static final String TAG = MainActivity.class.getSimpleName();
    private final CarsAdapter adapter = new CarsAdapter(this);      // adapter do listy samochodow
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();       // instancja firestore
    private final FirebaseStorage storage = FirebaseStorage.getInstance();      // instancja firestorage
    private final StorageReference storageRef = storage.getReference();
    @BindView(R.id.rvItems)
    RecyclerView rvItems;
    @BindView(R.id.pbMainLoading)
    ProgressBar pbMainLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // konfiguracja recycler view - widgetu do listy
        rvItems.setHasFixedSize(true);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        requestStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    // tworzenie menu w prawym gornym rogu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    // obsluga klikniec w menu w prawym gornym rogu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ladowanie samochodow z firestore
    private void loadItems() {
        pbMainLoading.setVisibility(View.VISIBLE);
        db.collection(Utils.CARS_COLLECTION)    // kolekcja cars
                .get()
                .addOnCompleteListener(task -> {
                    pbMainLoading.setVisibility(View.GONE);
                    // jesli ladowanie sie powiodlo
                    if (task.isSuccessful()) {
                        List<CarEntity> cars = new ArrayList<>();
                        // przejdz przez kazdy element i zapisz go do listy, mapując go na obiekt CarEntity
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            cars.add(document.toObject(CarEntity.class));
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        adapter.setData(cars);      // wstawienie danych do adaptera listy samochodow
                    } else {    // jesli pobranie sie nie powiodlo, wyswietl komunikat o bledzie
                        Toast.makeText(MainActivity.this, "Błąd podczas pobierania listy samochodów.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    // prosba o dostep do plikow przez aplikacje, jesli uzytkownik sie jeszcze nie zgodzil
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_REQUEST_CODE);
        }
    }

    // akcja uzytkownika przy dialogu o zgode na dostep do plikow
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] != PackageManager.PERMISSION_GRANTED)
            requestStoragePermission();
    }

    // klikniecie w przycisk 'dodaj' (plusik)
    @OnClick(R.id.fabMain)
    public void onFabMainClick() {
        startActivity(new Intent(this, NewCarActivity.class));
    }

    // otworzenie aktywnosci z szczegolami samochodu
    @Override
    public void onCarItemClick(int position) {
        Intent intent = new Intent(this, CarDetailsActivity.class);
        intent.putExtra(CarDetailsActivity.EXTRA_CAR_ITEM, adapter.getItem(position));  // przekazanie obiektu do aktywnosci
        startActivity(intent);
    }

    // usuniecie samochodu
    @Override
    public void onCarItemDelete(int position) {
        CarEntity item = adapter.getItem(position);

        // pobranie obiektu i usuniecie go z firestore poprzez id
        db.collection(Utils.CARS_COLLECTION).document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeItem(position);
                    removeImage(item.getImage());   // usuniecie przypisanego obrazu
                    Toast.makeText(MainActivity.this, "Obiekt usunięty", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Wystapił błąd podczas usuwania obiektu", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting document", e);
                });
    }

    // usuniecie obrazu
    private void removeImage(String image) {
        if(image != null && !image.isEmpty()) {
            String filename = Utils.retrieveImageFilename(image);   // uzyskanie nazwy pliku z linku do obrazu
            StorageReference desertRef = storageRef.child("images/" + filename);

            desertRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Obraz usuniety"))
                    .addOnFailureListener(exception -> Log.e(TAG, "Obraz nie został usuniety", exception));
        }
    }

    // edycja samochodu
    @Override
    public void onCarItemEdit(int position) {
        Intent intent = new Intent(this, EditCarActivity.class);
        intent.putExtra(CarDetailsActivity.EXTRA_CAR_ITEM, adapter.getItem(position));
        startActivity(intent);
    }
}