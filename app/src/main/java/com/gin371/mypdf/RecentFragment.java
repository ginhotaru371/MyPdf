package com.gin371.mypdf;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment implements OnPdfFileSelectListener {

    View view;
    ArrayList<FilePath> filePaths = new ArrayList<>();

    private static final String FILE_NAME = "recent.json";
    private RecyclerView List_pdf;
    private List<File> pdfList;
    private PDFAdapter pdfAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean fileChecker = false;
    String[] items = {"Rename", "Delete", "Favorites"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent, container, false);

        List_pdf = (RecyclerView) view.findViewById(R.id.list_recent_pdf);
        List_pdf.setHasFixedSize(true);
        List_pdf.setLayoutManager(new LinearLayoutManager(getContext()));
        List_pdf.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        displayPDF();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.recent_swiper_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayPDF();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void loadCurrentRecentList() {
        FileInputStream fileIn = null;

        try {
            fileIn = getActivity().openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fileIn);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String json;

            while ((json = bufferedReader.readLine()) != null) {
                stringBuilder.append(json);
            }

            Type type = new TypeToken<ArrayList<FilePath>>(){}.getType();
            Gson gson = new Gson();
            filePaths = gson.fromJson(stringBuilder.toString(), type);


            fileIn.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getPath() {
        FileInputStream file = null;

        try {
            file = view.getContext().openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(file);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String json;

            while ((json = bufferedReader.readLine()) != null) {
                stringBuilder.append(json);
            }

            Type type = new TypeToken<ArrayList<FilePath>>(){}.getType();
            Gson gson = new Gson();
            filePaths = gson.fromJson(stringBuilder.toString(), type);

            file.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayPDF() {
        pdfList = new ArrayList<>();
        getPath();
        try {
            if (filePaths.size() <= 10) {
                for (int i = filePaths.size() - 1; i >= 0; i--) {
                    File file = new File(filePaths.get(i).getFilePath());
                    if (file.exists()) {
                        pdfList.add(file);
                    }
                }
            } else {
                for (int i = filePaths.size() - 1; i >= filePaths.size() - 10; i--) {
                    File file = new File(filePaths.get(i).getFilePath());
                    if (file.exists()) {
                        pdfList.add(file);
                    }
                }
            }
        }
         catch (Exception e) {
            Toast.makeText(view.getContext(), "Nothing here", Toast.LENGTH_SHORT).show();
        }
        pdfAdapter = new PDFAdapter(view.getContext(), pdfList, this);
        List_pdf.setAdapter(pdfAdapter);
    }


    @Override
    public void onPdfSelected(File file) {
        Intent intent = new Intent(view.getContext(), PDFActivity.class);
        intent.putExtra("path", file.getAbsolutePath());
        startActivity(intent);
    }

    @Override
    public void onPdfLongClick(File file, int position) {
    }
}