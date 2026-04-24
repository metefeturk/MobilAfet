package com.example.mobilafet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilafet.R;
import com.example.mobilafet.models.AppNotification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<AppNotification> list;

    public NotificationAdapter(List<AppNotification> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppNotification notif = list.get(position);
        holder.title.setText(notif.title);
        holder.message.setText(notif.message);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("tr"));
        holder.date.setText(sdf.format(new Date(notif.createdAt)));
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_notif_title);
            message = itemView.findViewById(R.id.tv_notif_message);
            date = itemView.findViewById(R.id.tv_notif_date);
        }
    }
}