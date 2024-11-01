package com.example.cats_catch_mice.ui.itemList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import com.squareup.picasso.Picasso;
import com.example.cats_catch_mice.R;
import android.os.Bundle;
import androidx.navigation.Navigation;
import com.example.cats_catch_mice.databinding.ItemListRowBinding;
import java.util.List;

/*
    Handles single items in the fragment_item_list
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemListRowBinding binding = ItemListRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        Item item = items.get(position);

        holder.binding.itemListNameTextView.setText(item.getName());
        holder.binding.itemListCountTextView.setText("Count: " + String.valueOf(item.getCount()));
        holder.binding.itemListImageView.setImageResource(item.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemName", item.getName());
            bundle.putString("itemDescription", item.getDescription());
            bundle.putInt("itemCount", item.getCount());
            bundle.putInt("itemImage", item.getImageResId());

            // Use Navigation component to navigate to the detail fragment
            Navigation.findNavController(v).navigate(R.id.action_itemListFragment_to_itemDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemListRowBinding binding;

        public  ItemViewHolder(ItemListRowBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
