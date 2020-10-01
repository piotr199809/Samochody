package com.example.samochody.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.samochody.R;
import com.example.samochody.model.Utils;
import com.example.samochody.model.db.CarEntity;
import com.example.samochody.ui.adapter.CarItemListener;
import com.example.samochody.ui.adapter.CarsAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements CarItemListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();       // baza danych
    private final FirebaseStorage storage = FirebaseStorage.getInstance();      // firebase storage
    private final StorageReference storageRef = storage.getReference();
    private final CarsAdapter adapter = new CarsAdapter(this);
    private final List<String> carTypes = Arrays.asList("suv", "coupe", "dual cowl", "fastback", "hatchback", "kabriolet", "kombi", "liftback", "limuzyna", "mikrovan", "minivan", "pick-up", "roadster", "sedan", "targa", "van");
    private final List<String> engineTypes = Arrays.asList("benzyna", "diesel", "elektryczny");

    @BindView(R.id.tvSearchNoData)
    TextView tvSearchNoData;
    @BindView(R.id.rvSearchResults)
    RecyclerView rvSearchResults;
    @BindView(R.id.etSearch)
    EditText etSearch;
    @BindView(R.id.spSearchField)
    Spinner spSearchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        // konfiguracja recyclerview
        rvSearchResults.setHasFixedSize(true);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);
    }

    // zapytanie do bazy firebase
    private void makeQuery(String field, Object query) {
        tvSearchNoData.setVisibility(View.GONE);

        db.collection(Utils.CARS_COLLECTION)
                .whereEqualTo(field, query)     // zapytanie field == query
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {  // zapytanie zakonczone sukcesem

                        List<CarEntity> cars = new ArrayList<>();
                        // iteracja po obiektach i konwersja na obiekt samochodu
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            cars.add(document.toObject(CarEntity.class));
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                        adapter.setData(cars);

                        // jesli brak wynikow
                        if(cars.isEmpty()) {
                            tvSearchNoData.setVisibility(View.VISIBLE);
                            tvSearchNoData.setText(R.string.no_data);
                        }
                    } else {
                        tvSearchNoData.setVisibility(View.VISIBLE);
                        tvSearchNoData.setText(R.string.firebase_error);
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    @OnClick(R.id.bSearch)
    public void onSearchClick() {
        int item = spSearchField.getSelectedItemPosition();
        String query = etSearch.getText().toString();

        if(query.isEmpty()) {
            etSearch.setError("To pole nie może być puste");
            return;
        }

        // rodzaj pola dla zapytania w zaleznosci od wyboru w spinnerze
        switch (item) {
            case 0: {   // Marka
                makeQuery("brand", query);
                break;
            }
            case 1: {   // Model
                makeQuery("model", query);
                break;
            }
            case 2: {   // Rok produkcji
                try {
                    int productionYear = Integer.parseInt(query);
                    makeQuery("productionYear", productionYear);
                } catch (Exception ex) {
                    etSearch.setError("Proszę wpisać poprawny rok");
                }
                break;
            }
            case 3: {   // Typ nadwozia
                query = query.toLowerCase();
                int position = carTypes.indexOf(query);
                if(position == -1)
                    etSearch.setError("Wpisz jeden z poniższych typów nadwozia: suv, coupe, dual cowl, fastback, hatchback, kabriolet, kombi, liftback, limuzyna, mikrovan, minivan, pick-up, roadster, sedan, targa, van");
                else
                    makeQuery("carType", position);
                break;
            }
            case 4: {   // Typ silnika
                query = query.toLowerCase();
                int position = engineTypes.indexOf(query);
                if(position == -1)
                    etSearch.setError("Wpisz jeden z poniższych typów silnika: benzyna, diesel, elektryczny");
                else
                    makeQuery("engineType", position);
                break;
            }
        }
    }

    // tak samo jak w MainActivity
    @Override
    public void onCarItemClick(int position) {
        Intent intent = new Intent(this, CarDetailsActivity.class);
        intent.putExtra(CarDetailsActivity.EXTRA_CAR_ITEM, adapter.getItem(position));
        startActivity(intent);
    }

    // tak samo jak w MainActivity
    @Override
    public void onCarItemDelete(int position) {
        CarEntity item = adapter.getItem(position);

        db.collection(Utils.CARS_COLLECTION).document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeItem(position);
                    removeImage(item.getImage());
                    Toast.makeText(SearchActivity.this, "Obiekt usunięty", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SearchActivity.this, "Wystapił błąd podczas usuwania obiektu", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting document", e);
                });
    }

    // tak samo jak w MainActivity
    private void removeImage(String image) {
        if(image != null && !image.isEmpty()) {
            String filename = Utils.retrieveImageFilename(image);
            StorageReference desertRef = storageRef.child("images/" + filename);

            desertRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Obraz usuniety"))
                    .addOnFailureListener(exception -> Log.e(TAG, "Obraz nie został usuniety", exception));
        }
    }

    // tak samo jak w MainActivity
    @Override
    public void onCarItemEdit(int position) {
        Intent intent = new Intent(this, EditCarActivity.class);
        intent.putExtra(CarDetailsActivity.EXTRA_CAR_ITEM, adapter.getItem(position));
        startActivity(intent);
    }
}