package com.gin371.mypdf;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class PDFViewHolder extends RecyclerView.ViewHolder {
    public TextView tvName;
    public CardView container;

    public PDFViewHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.pdf_file_name);
        container = itemView.findViewById(R.id.file_container);
    }
}
