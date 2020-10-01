package com.example.samochody.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.bumptech.glide.Glide;
import com.example.samochody.R;
import com.example.samochody.model.db.CarEntity;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

// adapter do listy samochodow
public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.ViewHolder> {
    private List<CarEntity> items = new ArrayList<>();      // elementy listy
    private final CarItemListener listener;     // listener do interakcji z elementem

    public CarsAdapter(CarItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_car, parent, false);
        return new CarsAdapter.ViewHolder(v, listener);
    }

    // wypelnianie elementu listy danymi
    @Override
    public void onBindViewHolder(@NonNull CarsAdapter.ViewHolder holder, int position) {
        Context context = holder.ivCarItemPhoto.getContext();
        CarEntity item = items.get(position);

        String name = item.getBrand() + " " + item.getModel();
        holder.tvCarItemName.setText(name);

        // jesli jest obraz, pobierz go i wstaw
        if(item.getImage() != null && !item.getImage().isEmpty())
            Glide.with(context)
                    .load(item.getImage())
                    .into(holder.ivCarItemPhoto);
        else {  // jesli nie ma obrazu - wyswietl zdjecie zastepcze
            Drawable noImage = VectorDrawableCompat.create(holder.ivCarItemPhoto.getResources(), R.drawable.ic_round_image_24, null);
            holder.ivCarItemPhoto.setImageDrawable(noImage);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setData(List<CarEntity> cars) {
        items = cars;
        notifyDataSetChanged();
    }

    public CarEntity getItem(int position) {
        return items.get(position);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    // viewholder przetrzymuje widgety elementu listy
    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivCarItemPhoto)
        ImageView ivCarItemPhoto;
        @BindView(R.id.tvCarItemName)
        TextView tvCarItemName;

        public ViewHolder(@NonNull View itemView, CarItemListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.findViewById(R.id.bCarItemDelete).setOnClickListener(view -> listener.onCarItemDelete(getAdapterPosition()));
            itemView.findViewById(R.id.bCarItemEdit).setOnClickListener(view -> listener.onCarItemEdit(getAdapterPosition()));
            itemView.findViewById(R.id.bCarItem).setOnClickListener(view -> listener.onCarItemClick(getAdapterPosition()));
        }
    }
}
