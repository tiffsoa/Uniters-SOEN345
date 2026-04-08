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
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ---
// RecyclerView adapter for displaying customer reservations in MyReservationsActivity
// Event details (name, date, location) are resolved from a Map<eventID, Event> passed by the activity
// Shows both active and cancelled reservations with visual distinction
// ---

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(Reservation reservation);
    }

    private List<Reservation> reservations = new ArrayList<>();
    private Map<String, Event> eventMap = new HashMap<>();
    private final OnCancelClickListener listener;

    public ReservationAdapter(OnCancelClickListener listener) {
        this.listener = listener;
    }

    // Updates the adapter with reservation list and corresponding event details
    public void setData(List<Reservation> reservations, Map<String, Event> eventMap) {
        this.reservations = reservations;
        this.eventMap = eventMap;
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
        holder.bind(reservation, eventMap, listener);
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

        void bind(Reservation reservation, Map<String, Event> eventMap, OnCancelClickListener listener) {
            // Resolve event details from the event map using eventID
            Event event = eventMap.get(reservation.getEventID());
            if (event != null) {
                tvResEventName.setText(event.getName());
                tvResEventDate.setText(event.getFormattedDate());
                tvResEventLocation.setText(event.getLocation());
            } else {
                tvResEventName.setText("Unknown Event");
                tvResEventDate.setText("");
                tvResEventLocation.setText("");
            }

            tvResTicketCount.setText("Tickets: " + reservation.getTicketCount());
            tvResID.setText("ID: " + reservation.getReservationID());

            if (reservation.isIsCancelled()) {
                tvResStatus.setText("CANCELLED");
                tvResStatus.setTextColor(Color.parseColor("#C62828"));
                btnCancelReservation.setEnabled(false);
                btnCancelReservation.setVisibility(View.GONE);
                itemView.setAlpha(0.6f);
            } else {
                tvResStatus.setText("CONFIRMED");
                tvResStatus.setTextColor(Color.parseColor("#2E7D32"));
                btnCancelReservation.setEnabled(true);
                btnCancelReservation.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            }

            btnCancelReservation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(reservation);
                }
            });
        }
    }
}
