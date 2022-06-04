package com.example.mypasswordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PasswordListAdapter extends RecyclerView.Adapter<PasswordListAdapter.ViewHolder> {

    private final Context context;
    private final List<Password> passwordList;

    // Constructor
    public PasswordListAdapter(Context context, List<Password> passwordList) {
        this.context = context;
        this.passwordList = passwordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.passwords_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // to set data to textview  of each card layout
        Password password = passwordList.get(position);
        holder.label.setText(password.label);
        holder.website.setText(password.website);
        holder.copyButton.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", password.password);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied password to clipboard!", Toast.LENGTH_SHORT).show();
        });
        if (password.wasPwned) {
            holder.warningButton.setVisibility(View.VISIBLE);
        }
        holder.warningButton.setOnClickListener(view ->
                Toast.makeText(context, "Password must be updated due to security risk",
                Toast.LENGTH_LONG).show());
        holder.moreItemsButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(context, holder.moreItemsButton);
            popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.edit_password:
                        // TODO: Edit password
                        return true;
                    case R.id.delete_password:
                        // TODO: Delete password
                        return true;
                    default:
                        return false;
                }
            });
            popup.inflate(R.menu.password_more_actions_menu);
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }

    // View holder class for initializing of views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextView website;
        private final ImageButton copyButton, moreItemsButton, warningButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            website = itemView.findViewById(R.id.website);
            label = itemView.findViewById(R.id.label);
            moreItemsButton = itemView.findViewById(R.id.more_menu);
            copyButton = itemView.findViewById(R.id.copy_button);
            warningButton = itemView.findViewById(R.id.warning_drawable);
        }
    }
}

