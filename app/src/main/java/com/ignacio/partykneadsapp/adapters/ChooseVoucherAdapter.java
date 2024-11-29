package com.ignacio.partykneadsapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ignacio.partykneadsapp.R;
import com.ignacio.partykneadsapp.model.VoucherModel;

import java.util.List;

public class ChooseVoucherAdapter extends RecyclerView.Adapter<ChooseVoucherAdapter.ViewHolder> {

    private final Context context;
    private final List<VoucherModel> voucherList;
    private final VoucherClickListener listener; // Interface to communicate selection

    public ChooseVoucherAdapter(Context context, List<VoucherModel> voucherList, VoucherClickListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choosevoucher_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoucherModel voucher = voucherList.get(position);
        String percentDiscount = "";

        // Set the voucher details
        holder.tvDiscount.setText(voucher.getDiscount());

        // Customize based on the discount type
        if ("10% Discount".equals(voucher.getDiscount())) {
            holder.tvStatus.setText("You've got a sweet discount! Enjoy!");
            holder.image.setImageResource(R.drawable.vten);
            percentDiscount = "10%";
        } else if ("20% Discount".equals(voucher.getDiscount())) {
            holder.tvStatus.setText("You've got a sweet discount! Enjoy!");
            holder.image.setImageResource(R.drawable.vtwenty);
            percentDiscount = "20%";
        } else if ("15% Discount".equals(voucher.getDiscount())) {
            holder.tvStatus.setText("You've got a sweet discount! Enjoy!");
            holder.image.setImageResource(R.drawable.vfifteen);
            percentDiscount = "15%";
        } else if ("₱100 Off".equals(voucher.getDiscount())) {
            holder.tvStatus.setText("You've got a sweet discount! Enjoy!");
            holder.image.setImageResource(R.drawable.vone);
            percentDiscount = "₱100";
        } else if ("₱200 Off".equals(voucher.getDiscount())) {
            holder.tvStatus.setText("You've got a sweet discount! Enjoy!");
            holder.image.setImageResource(R.drawable.vtwo);
            percentDiscount = "₱200";
        }

        // Handle item clicks
        String finalPercentDiscount = percentDiscount;
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Voucher Selected: " + voucher.getDiscount(), Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onVoucherClick(finalPercentDiscount); // Pass the discount back
            }

            // Remove the item from Firestore
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String voucherId = voucher.getId(); // Assuming your VoucherModel has an ID field

            firestore.collection("Users")
                    .document(uid)
                    .collection("Vouchers")
                    .document(voucherId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove item locally and notify adapter
                        voucherList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, voucherList.size());
                        Toast.makeText(context, "Voucher removed successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChooseVoucherAdapter", "Error removing voucher", e);
                        Toast.makeText(context, "Failed to remove voucher", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiscount, tvStatus;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscount = itemView.findViewById(R.id.discount);
            tvStatus = itemView.findViewById(R.id.discountText);
            image = itemView.findViewById(R.id.image);
        }
    }

    public interface VoucherClickListener {
        void onVoucherClick(String discount);
    }
}
