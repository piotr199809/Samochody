package com.example.samochody.ui.adapter;

// listener do interakcji z elementem listy samochodow
public interface CarItemListener {
    void onCarItemClick(int position);
    void onCarItemDelete(int position);
    void onCarItemEdit(int position);
}
