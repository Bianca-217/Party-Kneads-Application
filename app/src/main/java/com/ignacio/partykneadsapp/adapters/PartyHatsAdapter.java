package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.PartyHatsModel;

import java.util.List;

public class PartyHatsAdapter extends RecyclerView.Adapter<PartyHatsAdapter.ViewHolder> {

    private Context context;
    private List<PartyHatsModel> hatList;
    private PartyHatsModel selectedHat;
    private OnHatClickListener onHatClickListener;

    // Define an interface for hat selection callback
    public interface OnHatClickListener {
        void onHatClick(String colorName);
    }

    public PartyHatsAdapter(Context context, List<PartyHatsModel> hatList, OnHatClickListener onHatClickListener) {
        this.context = context;
        this.hatList = hatList;
        this.selectedHat = hatList.get(0); // Set default selected hat if needed
        this.onHatClickListener = onHatClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cartoon_partyhats, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PartyHatsModel hat = hatList.get(position);

        holder.hatIcon.setImageResource(hat.getImage());
        holder.hatName.setText(hat.getThemeName()); // Changed to match PartyHatsModel getter

        Context context = holder.itemView.getContext();
        if (hat.equals(selectedHat)) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.hatName.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.hatName.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedHat = hat;
            notifyDataSetChanged();
            // Trigger callback with the selected hat name
            onHatClickListener.onHatClick(hat.getThemeName()); // Changed to match PartyHatsModel getter
        });
    }

    @Override
    public int getItemCount() {
        return hatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView hatIcon;
        TextView hatName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hatIcon = itemView.findViewById(R.id.hatIcon); // Update with your actual ID
            hatName = itemView.findViewById(R.id.hatName); // Update with your actual ID
            cardView = itemView.findViewById(R.id.cartoonCardview); // Update with your actual ID
        }
    }
}
