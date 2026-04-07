package com.example.ticketreservationapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Reservation;

import java.util.ArrayList;
import java.util.List;

// ---
// RecyclerView adapter for displaying customer reservations in MyReservationsActivity
// Updates in real-time via firestore snapshot listener on Reservations collection filtered by customerID
// ---

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(Reservation reservation);
    }

    private List<Reservation> reservations = new ArrayList<>();
    private final OnCancelClickListener listener;

    public ReservationAdapter(OnCancelClickListener listener) {
        this.listener = listener;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.bind(reservation, listener);
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvResEventName;
        private final TextView tvResStatus;
        private final TextView tvResEventDate;
        private final TextView tvResEventLocation;
        private final TextView tvResTicketCount;
        private final TextView tvResID;
        private final Button btnCancelReservation;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResEventName = itemView.findViewById(R.id.tvResEventName);
            tvResStatus = itemView.findViewById(R.id.tvResStatus);
            tvResEventDate = itemView.findViewById(R.id.tvResEventDate);
            tvResEventLocation = itemView.findViewById(R.id.tvResEventLocation);
            tvResTicketCount = itemView.findViewById(R.id.tvResTicketCount);
            tvResID = itemView.findViewById(R.id.tvResID);
            btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
        }

        void bind(Reservation reservation, OnCancelClickListener listener) {
            tvResEventName.setText(reservation.getEventName());
            tvResEventDate.setText("📅 " + reservation.getEventDate());
            tvResEventLocation.setText("📍 " + reservation.getEventLocation());
            tvResTicketCount.setText("🎟 Tickets: " + reservation.getQuantity());
            tvResID.setText("ID: " + reservation.getReservationID());

            if (reservation.isConfirmed()) {
                tvResStatus.setText("CONFIRMED");
                tvResStatus.setTextColor(Color.parseColor("#2E7D32")); // green
                btnCancelReservation.setEnabled(true);
                btnCancelReservation.setVisibility(View.VISIBLE);
            } else {
                tvResStatus.setText("CANCELED");
                tvResStatus.setTextColor(Color.parseColor("#C62828")); // red
                btnCancelReservation.setEnabled(false);
                btnCancelReservation.setVisibility(View.GONE);
            }

            btnCancelReservation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(reservation);
                }
            });
        }
    }
}
