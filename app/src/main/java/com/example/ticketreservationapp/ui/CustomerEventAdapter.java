package com.example.ticketreservationapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerEventAdapter extends RecyclerView.Adapter<CustomerEventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Event event);
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvName.setText(event.getName());
        holder.tvLocation.setText(event.getLocation());
        holder.tvCategory.setText(event.getCategory());
        holder.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.getDate()));

        int remaining = event.getCapacity() - event.getBookedSeats();
        holder.tvSeats.setText("Available: " + remaining + " / " + event.getCapacity());

        if (event.isCancelled()) {
            holder.tvCancelled.setVisibility(View.VISIBLE);
            holder.btnBook.setVisibility(View.GONE);
        } else if (remaining <= 0) {
            holder.tvCancelled.setVisibility(View.GONE);
            holder.btnBook.setVisibility(View.VISIBLE);
            holder.btnBook.setText("Sold Out");
            holder.btnBook.setEnabled(false);
        } else {
            holder.tvCancelled.setVisibility(View.GONE);
            holder.btnBook.setVisibility(View.VISIBLE);
            holder.btnBook.setText("Book");
            holder.btnBook.setEnabled(true);
        }

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) listener.onBookClick(event);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvCategory, tvDate, tvSeats, tvCancelled;
        Button btnBook;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustomerEventName);
            tvLocation = itemView.findViewById(R.id.tvCustomerEventLocation);
            tvCategory = itemView.findViewById(R.id.tvCustomerEventCategory);
            tvDate = itemView.findViewById(R.id.tvCustomerEventDate);
            tvSeats = itemView.findViewById(R.id.tvCustomerEventSeats);
            tvCancelled = itemView.findViewById(R.id.tvCustomerEventCancelled);
            btnBook = itemView.findViewById(R.id.btnBookEvent);
        }
    }
}
