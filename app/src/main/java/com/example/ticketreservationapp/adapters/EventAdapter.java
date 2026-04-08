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

import java.util.ArrayList;
import java.util.List;

// ---
// RecyclerView adapter for displaying events in the Events Catalog screen
// Shows all events including cancelled ones (cancelled events are greyed out with booking disabled)
// Updates in real-time via firestore snapshot listener on Events collection
// ---

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final OnBookClickListener listener;

    public EventAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvEventCategory;
        private final TextView tvEventDate;
        private final TextView tvEventLocation;
        private final TextView tvEventCapacity;
        private final TextView tvEventStatus;
        private final Button btnBookNow;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventCapacity = itemView.findViewById(R.id.tvEventCapacity);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);
            btnBookNow = itemView.findViewById(R.id.btnBookNow);
        }

        void bind(Event event, OnBookClickListener listener) {
            tvEventName.setText(event.getName());
            tvEventCategory.setText(event.getCategory());
            tvEventDate.setText(event.getFormattedDate());
            tvEventLocation.setText(event.getLocation());
            tvEventCapacity.setText(event.getRemainingCapacity() + " / " + event.getMaxCapacity() + " seats available");

            if (event.isIsCancelled()) {
                tvEventStatus.setVisibility(View.VISIBLE);
                tvEventStatus.setText("CANCELLED");
                tvEventStatus.setTextColor(Color.parseColor("#C62828"));
                btnBookNow.setEnabled(false);
                btnBookNow.setText("Unavailable");
                itemView.setAlpha(0.6f);
            } else if (event.getRemainingCapacity() <= 0) {
                tvEventStatus.setVisibility(View.GONE);
                btnBookNow.setEnabled(false);
                btnBookNow.setText("Sold Out");
                itemView.setAlpha(1.0f);
            } else {
                tvEventStatus.setVisibility(View.GONE);
                btnBookNow.setEnabled(true);
                btnBookNow.setText("Book Now");
                itemView.setAlpha(1.0f);
            }

            btnBookNow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(event);
                }
            });
        }
    }
}
