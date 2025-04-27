package com.example.ss_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList;
    private String userType;

    public ChatAdapter(List<ChatMessage> messageList, String userType) {
        this.messageList = messageList;
        this.userType = userType;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        return message.getSender().equals(userType) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        SentMessageHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }

        void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }

        void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
        }
    }
}
