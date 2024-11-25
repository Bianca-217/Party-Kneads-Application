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
import com.ignacio.partykneadsapp.model.BannerColorModel;

import java.util.List;

public class BannerColorAdapter extends RecyclerView.Adapter<BannerColorAdapter.ViewHolder> {

    private Context context;
    private List<BannerColorModel> bannerList;
    private BannerColorModel selectedBanner;
    private OnBannerClickListener onBannerClickListener;

    // Define an interface for banner selection callback
    public interface OnBannerClickListener {
        void onBannerClick(String bannerName);
    }

    public BannerColorAdapter(Context context, List<BannerColorModel> bannerList, OnBannerClickListener onBannerClickListener) {
        this.context = context;
        this.bannerList = bannerList;
        this.selectedBanner = bannerList.get(0); // Set default selected banner if needed
        this.onBannerClickListener = onBannerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_colors, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BannerColorModel banner = bannerList.get(position);

        holder.bannerIcon.setImageResource(banner.getImage());
        holder.bannerName.setText(banner.getColorName());

        Context context = holder.itemView.getContext();
        if (banner.equals(selectedBanner)) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink));
            holder.bannerName.setTextColor(ContextCompat.getColor(context, R.color.semiwhite));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.footerpink));
            holder.bannerName.setTextColor(ContextCompat.getColor(context, R.color.semiblack));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedBanner = banner;
            notifyDataSetChanged();
            // Trigger callback with the selected banner name
            onBannerClickListener.onBannerClick(banner.getColorName());
        });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerIcon;
        TextView bannerName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerIcon = itemView.findViewById(R.id.colorIcon);
            bannerName = itemView.findViewById(R.id.colorText);
            cardView = itemView.findViewById(R.id.colorCardView);
        }
    }
}
