package com.egeperk.chatappjava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<String> chatMessages;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String chatMessage = chatMessages.get(position);
        holder.chatMessage.setText(chatMessage);

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    public RecyclerViewAdapter(List<String> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView chatMessage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatMessage = itemView.findViewById(R.id.recycler_text_view);
        }
    }


}
