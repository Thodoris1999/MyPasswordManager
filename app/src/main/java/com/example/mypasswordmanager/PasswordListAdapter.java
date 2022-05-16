package com.example.mypasswordmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PasswordListAdapter extends RecyclerView.Adapter<PasswordListAdapter.Viewholder> {

    private Context context;
    private ArrayList<PasswordData> passwordArrayList;

    // Constructor
    public PasswordListAdapter(Context context, ArrayList<PasswordData> passwordArrayList) {
        this.context = context;
        this.passwordArrayList = passwordArrayList;
    }

    @NonNull
    @Override
    public PasswordListAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passwords_lisf, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordListAdapter.Viewholder holder, int position) {
        // to set data to textview  of each card layout
        PasswordData model = passwordArrayList.get(position);
        holder.label.setText(model.getLabel());
        holder.password.setText(model.getPassword());
        holder.website.setText(model.getWebsite());
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return passwordArrayList.size();
    }

    // View holder class for initializing of views
    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView label, password, website;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            website = itemView.findViewById(R.id.website);
            label = itemView.findViewById(R.id.label);
            password = itemView.findViewById(R.id.password);
        }
    }
}

