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
import com.ignacio.partykneadsapp.model.BalloonColorModel;

import java.util.List;

public class BalloonColorAdapter extends RecyclerView.Adapter<BalloonColorAdapter.ViewHolder> {

    private Context context;
    private List<BalloonColorModel> colorList;
    private BalloonColorModel selectedColor;
    private OnColorClickListener onColorClickListener;

    // Define an interface for color selection callback
    public interface OnColorClickListener {
        void onColorClick(String colorName);
    }

    public BalloonColorAdapter(Context context, List<BalloonColorModel> colorList, OnColorClickListener onColorClickListener) {
        this.context = context;
        this.colorList = colorList;
        this.selectedColor = colorList.get(0); // Set default selected color if needed
        this.onColorClickListener = onColorClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.balloon_colors, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BalloonColorModel color = colorList.get(position);

        holder.colorIcon.setImageResource(color.getImage());
        holder.colorName.setText(color.getColorName());

        Context context = holder.itemView.getContext();
        if (color.equals(selectedColor)) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.colorName.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.colorName.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedColor = color;
            notifyDataSetChanged();
            // Trigger callback with the selected color name
            onColorClickListener.onColorClick(color.getColorName());
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView colorIcon;
        TextView colorName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIcon = itemView.findViewById(R.id.colorIcon);
            colorName = itemView.findViewById(R.id.colorText);
            cardView = itemView.findViewById(R.id.colorCardView);
        }
    }
}
