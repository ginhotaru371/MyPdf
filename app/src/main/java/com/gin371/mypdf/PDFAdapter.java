package com.gin371.mypdf;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDFAdapter extends RecyclerView.Adapter<PDFViewHolder> implements Filterable {
    private Context context;
    private List<File> pdfFiles;
    private List<File> pdfFilesNew;
    OnPdfFileSelectListener listener;

    public PDFAdapter(Context context, List<File> pdfFiles, OnPdfFileSelectListener listener) {
        this.context = context;
        this.pdfFiles = pdfFiles;
        this.pdfFilesNew = pdfFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PDFViewHolder(LayoutInflater.from(context).inflate(R.layout.file_holder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PDFViewHolder holder, int position) {
        holder.tvName.setText(pdfFiles.get(position).getName());
        holder.tvName.setSelected(true);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPdfSelected(pdfFiles.get(holder.getBindingAdapterPosition()));
            }
        });
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onPdfLongClick(pdfFiles.get(holder.getBindingAdapterPosition()), holder.getBindingAdapterPosition());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return pdfFiles.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if (strSearch.isEmpty()) {
                    pdfFilesNew = pdfFiles;
                } else {
                    List<File> listTemp = new ArrayList<>();
                    for (File file : pdfFiles) {
                        if (file.getName().toLowerCase().contains(strSearch.toLowerCase())) {
                            listTemp.add(file);
                        }
                    }
                    pdfFilesNew = listTemp;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = pdfFilesNew;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                pdfFilesNew = (List<File>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void filterList(List<File> filterList) {
        pdfFiles = filterList;
        notifyDataSetChanged();
    }
}