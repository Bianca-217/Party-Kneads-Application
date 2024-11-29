package com.ignacio.partykneadsapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private Context context;
    private List<VoucherModel> voucherList;

    public VoucherAdapter(Context context, List<VoucherModel> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the voucher_items layout
        View view = LayoutInflater.from(context).inflate(R.layout.voucher_items, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        VoucherModel voucher = voucherList.get(position);

        // Set discount text
        holder.discount.setText(voucher.getDiscount());

        // Apply conditions for discountText and image
        if ("10% Discount".equals(voucher.getDiscount())) {
            holder.discountText.setText("You've got a sweet discount enjoy!");
            holder.image.setImageResource(R.drawable.vten); // Replace with the actual drawable resource
        } else if ("20% Discount".equals(voucher.getDiscount())) {
            holder.discountText.setText("You've got a sweet discount enjoy!");
            holder.image.setImageResource(R.drawable.vtwenty);
        } else if ("15% Discount".equals(voucher.getDiscount())) {
            holder.discountText.setText("You've got a sweet discount enjoy!");
            holder.image.setImageResource(R.drawable.vfifteen);
        } else if ("₱100 Off".equals(voucher.getDiscount())) {
            holder.discountText.setText("You've got a sweet discount enjoy!");
            holder.image.setImageResource(R.drawable.vone);
        } else if ("₱200 Off".equals(voucher.getDiscount())) {
            holder.discountText.setText("You've got a sweet discount enjoy!");
            holder.image.setImageResource(R.drawable.vtwo);
        }

        // Handle claim button
        holder.btnClaim.setOnClickListener(v -> {
            // Show confirmation dialog
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Claim Voucher")

                    .setMessage("Are you sure you want to claim this voucher?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Update voucher status to "claimed" in Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(uid)
                                .collection("Vouchers").document(voucher.getId()) // Assuming voucher ID is stored in VoucherModel
                                .update("status", "claimed")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Voucher claimed successfully!", Toast.LENGTH_SHORT).show();
                                    voucher.setStatus("claimed");
                                    notifyItemChanged(holder.getAdapterPosition());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to claim voucher. Please try again.", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView discount, discountText;
        ImageView image;
        Button btnClaim;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            discount = itemView.findViewById(R.id.discount);
            discountText = itemView.findViewById(R.id.discoutText);
            image = itemView.findViewById(R.id.image);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }
    }
}
