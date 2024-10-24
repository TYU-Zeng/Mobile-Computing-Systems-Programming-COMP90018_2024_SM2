package com.example.cats_catch_mice.ui.itemList;

//public class ItemAdapter {
//}
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
//public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
//
//    private List<Item> itemList;
//    private OnItemClickListener onItemClickListener;
//
//    public ItemAdapter(List<Item> itemList, OnItemClickListener onItemClickListener) {
//        this.itemList = itemList;
//        this.onItemClickListener = onItemClickListener;
//    }
//
//    @NonNull
//    @Override
//    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Inflate the item layout (item_list_row.xml)
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);
//        return new ItemViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
//        // Bind the data to the views
//        Item item = itemList.get(position);
//        holder.itemNameTextView.setText(item.getName());
//        holder.itemCountTextView.setText("Count: " + item.getCount());
//        holder.itemImageView.setImageResource(item.getImageResId()); // If using drawable
//
//        // Handle item click
//        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
//    }
//
//    @Override
//    public int getItemCount() {
//        return itemList.size();
//    }
//
//    // ViewHolder class
//    public static class ItemViewHolder extends RecyclerView.ViewHolder {
//        ImageView itemImageView;
//        TextView itemNameTextView;
//        TextView itemCountTextView;
//
//        public ItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            itemImageView = itemView.findViewById(R.id.itemImageView);
//            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
//            itemCountTextView = itemView.findViewById(R.id.itemCountTextView);
//        }
//    }
//
//    // Interface for handling clicks
//    public interface OnItemClickListener {
//        void onItemClick(Item item);
//    }
//}

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

//    private List<Item> itemList;
//    private OnItemClickListener onItemClickListener;
    private List<Item> items;

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }
//    public interface OnItemClickListener {
//        void onItemClick(Item item);
//    }
//
//    public ItemAdapter(OnItemClickListener onItemClickListener) {
//        this.onItemClickListener = onItemClickListener;
//    }

    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item_list, parent, false);
//        return new ItemViewHolder(view);
        ItemListRowBinding binding = ItemListRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        Item item = items.get(position);
//        holder.itemName.setText(item.getName());
//        holder.itemCount.setText(String.valueOf(item.getCount()));
//        holder.itemImage.setImageResource(item.getImageResId());

        holder.binding.itemListNameTextView.setText(item.getName());
        holder.binding.itemListCountTextView.setText(String.valueOf(item.getCount()));
        holder.binding.itemListImageView.setImageResource(item.getImageResId());

//        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("itemName", item.getName());
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

//        TextView itemName;
//        TextView itemCount;
//        ImageView itemImage
//
//        public ItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            itemName = itemView.findViewById(R.id.itemListNameTextView);
//            itemCount = itemView.findViewById(R.id.itemListCountTextView);
//            itemImage = itemView.findViewById(R.id.itemListImageView);
//        }
        ItemListRowBinding binding;

        public  ItemViewHolder(ItemListRowBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
