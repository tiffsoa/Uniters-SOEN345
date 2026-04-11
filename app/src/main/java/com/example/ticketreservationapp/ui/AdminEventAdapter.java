package com.example.ticketreservationapp.ui;

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
        holder.tvDetails.setText(event.getLocation() + " - " + event.getCategory() + " - Capacity: " + event.getCapacity());
        holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.getDate()));
        holder.tvEventID.setText("Event ID: " + event.getEventID());
        holder.tvCancelled.setVisibility(event.isCancelled() ? View.VISIBLE : View.GONE);

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
        TextView tvName, tvDetails, tvDate, tvEventID, tvCancelled;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEventName);
            tvDetails = itemView.findViewById(R.id.tvEventDetails);
            tvDate = itemView.findViewById(R.id.tvEventDate);
            tvEventID = itemView.findViewById(R.id.tvEventID);
            tvCancelled = itemView.findViewById(R.id.tvCancelled);
        }
    }
}