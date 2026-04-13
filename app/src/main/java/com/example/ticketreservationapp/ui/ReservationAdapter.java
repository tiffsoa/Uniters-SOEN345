package com.example.ticketreservationapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private List<Reservation> reservations = new ArrayList<>();
    private OnCancelClickListener listener;

    // Maps event IDs to event names for display purposes
    private java.util.Map<String, String> eventNameMap = new java.util.HashMap<>();
    private java.util.Map<String, com.example.ticketreservationapp.models.Event> eventMap = new java.util.HashMap<>();

    public interface OnCancelClickListener {
        void onCancelClick(Reservation reservation);
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.listener = listener;
    }

    public void setEventNameMap(java.util.Map<String, String> map) {
        this.eventNameMap = map;
    }

    public void setEventMap(java.util.Map<String, com.example.ticketreservationapp.models.Event> map) {
        this.eventMap = map;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation res = reservations.get(position);

        String eventName = eventNameMap.getOrDefault(res.getEventID(), "Event: " + res.getEventID());
        holder.tvEventName.setText(eventName);

        com.example.ticketreservationapp.models.Event event = eventMap.get(res.getEventID());
        if (event != null) {
            holder.tvCategory.setText("Category: " + event.getCategory());
            holder.tvLocation.setText("Location: " + event.getLocation());
            holder.tvEventDate.setText("Date: " +
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.getDate()));
        } else {
            holder.tvCategory.setText("");
            holder.tvLocation.setText("");
            holder.tvEventDate.setText("");
        }

        holder.tvTicketCount.setText("Tickets: " + res.getTicketCount());
        holder.tvReservationID.setText("Reservation: " + res.getReservationID());

        if (res.getCreatedAt() != null) {
            holder.tvCreatedAt.setText("Booked: " +
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(res.getCreatedAt()));
        } else {
            holder.tvCreatedAt.setText("");
        }

        if (res.isCancelled()) {
            holder.tvStatus.setText("Cancelled");
            holder.tvStatus.setTextColor(0xFFFF0000);
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(0xFF4CAF50);
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancelClick(res);
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public void setReservations(List<Reservation> newList) {
        this.reservations = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvCategory, tvLocation, tvEventDate, tvTicketCount, tvReservationID, tvCreatedAt, tvStatus;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvResEventName);
            tvCategory = itemView.findViewById(R.id.tvResCategory);
            tvLocation = itemView.findViewById(R.id.tvResLocation);
            tvEventDate = itemView.findViewById(R.id.tvResEventDate);
            tvTicketCount = itemView.findViewById(R.id.tvResTicketCount);
            tvReservationID = itemView.findViewById(R.id.tvResID);
            tvCreatedAt = itemView.findViewById(R.id.tvResCreatedAt);
            tvStatus = itemView.findViewById(R.id.tvResStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelReservation);
        }
    }
}
