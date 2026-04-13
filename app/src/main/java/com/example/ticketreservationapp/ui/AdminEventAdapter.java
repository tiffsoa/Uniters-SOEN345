package com.example.ticketreservationapp.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvName.setText(event.getName());
        holder.tvEventID.setText("Event ID: " + event.getEventID());
        holder.tvCategory.setText("Category: " + event.getCategory());
        holder.tvLocation.setText("Location: " + event.getLocation());
        holder.tvDate.setText("Date: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.getDate()));

        int remaining = event.getCapacity() - event.getBookedSeats();
        holder.tvSeats.setText("Available: " + remaining + " / " + event.getCapacity());
        holder.tvSeats.setTextColor(remaining <= 0 ? Color.RED : Color.parseColor("#1976D2"));

        if (event.isCancelled()) {
            holder.tvCancelled.setVisibility(View.VISIBLE);
            holder.card.setCardBackgroundColor(0xFFE0E0E0);
        } else {
            holder.tvCancelled.setVisibility(View.GONE);
            holder.card.setCardBackgroundColor(0xFFFFFFFF);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvLocation, tvDate, tvSeats, tvEventID, tvCancelled;
        com.google.android.material.card.MaterialCardView card;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.adminEventCard);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvSeats = itemView.findViewById(R.id.tvEventSeats);
            tvEventID = itemView.findViewById(R.id.tvEventID);
            tvCancelled = itemView.findViewById(R.id.tvCancelled);
        }
    }
}