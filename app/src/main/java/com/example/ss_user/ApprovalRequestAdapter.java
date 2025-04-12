package com.example.ss_user;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ApprovalRequestAdapter extends RecyclerView.Adapter<ApprovalRequestAdapter.ViewHolder> {

    private List<ApprovalRequest> requestList;
    private Context context;
    public ApprovalRequestAdapter(List<ApprovalRequest> list, Context ctx) {
        this.requestList = list;
        this.context = ctx;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvDate, tvReason, tvStatus;
        Button btnAccept;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApprovalRequest request = requestList.get(position);
        holder.tvUserId.setText("User: " + request.userId);
        holder.tvDate.setText("Date: " + request.requestedDate);
        holder.tvReason.setText("Reason: " + request.reason);
        holder.tvStatus.setText("Status: " + request.status);

        holder.btnAccept.setOnClickListener(v -> {
            // Update status in database
            updateRequestStatus(request, "accepted");

            // Navigate to new activity
            Intent intent = new Intent(context, expenses_history_edit_for_old_date.class);
            intent.putExtra("userId", request.userId);
            intent.putExtra("requestedDate", request.requestedDate);
            intent.putExtra("userType", request.userType);
            context.startActivity(intent);
        });
    }


    private void updateRequestStatus(ApprovalRequest request, String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(request.userType)
                .child(request.userId)
                .child("Requests");

        // You need the request key to update status — assuming status is unique, or you can send ID
        ref.orderByChild("timestamp").equalTo(request.timestamp)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot reqSnap : snapshot.getChildren()) {
                            reqSnap.getRef().child("status").setValue(status);
                            Toast.makeText(context, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }
}
