package com.gin371.mypdf;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

public class FavoriteFragment extends Fragment implements OnPdfFileSelectListener {

    View view;
    ArrayList<FilePath> filePaths = new ArrayList<>();

    private static final String FILE_NAME = "favorite.json";
    private RecyclerView List_pdf;
    private List<File> pdfList;
    private PDFAdapter pdfAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean fileChecker = false;
    String[] items = {"Rename", "Delete", "Favorites"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorite, container, false);

        List_pdf = (RecyclerView) view.findViewById(R.id.list_favorite_pdf);
        List_pdf.setHasFixedSize(true);
        List_pdf.setLayoutManager(new LinearLayoutManager(getContext()));
        List_pdf.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        displayPDF();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiper_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                displayPDF();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void loadCurrentFavoriteList() {
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

    private void checkFileInFavoriteList(String filePath) {
        loadCurrentFavoriteList();
        if (filePaths != null) {
            for (FilePath singleFilePath : filePaths) {
                if (singleFilePath.getFilePath().equals(filePath)) {
                    fileChecker = true;
                    break;
                } else {
                    fileChecker = false;
                }
            }
        }
        System.out.println(fileChecker);

    }

    private void addFavorites(File file) {
        FileOutputStream fileOut = null;
        FilePath filePath = new FilePath(file.getAbsolutePath());
        ArrayList<FilePath> newFilePaths = new ArrayList<>();
        newFilePaths.add(filePath);
        if (filePaths != null) {
            filePaths.addAll(newFilePaths);
        } else {
            filePaths = newFilePaths;
        }
        String json = null;

        Gson gson = new Gson();
        json = gson.toJson(filePaths);

        try {
            fileOut = getActivity().openFileOutput(FILE_NAME, getContext().MODE_PRIVATE);
            fileOut.write(json.getBytes());
            fileOut.close();

            Toast.makeText(getContext(), "Added to Favorite List", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void removeFavorites(File file) {
        FileOutputStream fileOut = null;

        for (int i = 0; i < filePaths.size(); i++ ) {
            if (filePaths.get(i).getFilePath().equals(file.getAbsolutePath())) {
                filePaths.remove(filePaths.get(i));
                fileChecker = false;
            }
        }

        String json = null;
        Gson gson = new Gson();
        json = gson.toJson(filePaths);

        try {
            fileOut = getActivity().openFileOutput(FILE_NAME, getContext().MODE_PRIVATE);
            fileOut.write(json.getBytes());
            fileOut.close();

            Toast.makeText(getContext(), "Remove from Favorite List", Toast.LENGTH_SHORT).show();
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
            for (FilePath singleFilePath : filePaths) {
                File file = new File(singleFilePath.getFilePath());
                if (file.exists()) {
                    pdfList.add(file);
                }
            }
        } catch (Exception e) {
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

        checkFileInFavoriteList(file.getAbsolutePath());

        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select Options");
        ListView options = (ListView) optionDialog.findViewById(R.id.list_option);
        FavoriteFragment.CustomAdapter customAdapter = new FavoriteFragment.CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();

        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                String selectedItem = parent.getItemAtPosition(i).toString();

                switch (selectedItem) {
                    case "Rename":
                        AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("Rename File");
                        final EditText name = new EditText(getContext());
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String new_name = name.getEditableText().toString();
                                String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                File current = new File(file.getAbsolutePath());
                                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name) + extention);

                                if (current.renameTo(destination)) {
                                    pdfList.set(position, destination);
                                    pdfAdapter.notifyItemChanged(position);
                                    Toast.makeText(getContext(), "Renamed", Toast.LENGTH_SHORT).show();
                                    optionDialog.cancel();
                                } else {
                                    Toast.makeText(getContext(), "Couldn't Rename", Toast.LENGTH_SHORT);
                                }
                            }
                        });

                        renameDialog.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });
                        AlertDialog alertdialog_rename = renameDialog.create();
                        alertdialog_rename.show();
                        break;
                    case "Delete":
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("Delete" + file.getName() + "?");
                        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                pdfList.remove(position);
                                pdfAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                optionDialog.cancel();
                            }
                        });
                        deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                optionDialog.cancel();
                            }
                        });
                        AlertDialog alertDialog_delete = deleteDialog.create();
                        alertDialog_delete.show();
                        break;
                    case "Favorites":
                        if (fileChecker) {
                            removeFavorites(file);
                            optionDialog.cancel();
                        } else {
                            addFavorites(file);
                            optionDialog.cancel();
                        }
                        break;
                }
            }
        });

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.option_layout, null);

            TextView txtOptions = view.findViewById(R.id.name_option);
            ImageView imgOptions = view.findViewById(R.id.icn_option);
            txtOptions.setText(items[position]);
            if(items[position].equals("Rename")) {
                imgOptions.setImageResource(R.drawable.icn_file_rename);
            } else if (items[position].equals("Delete")) {
                imgOptions.setImageResource(R.drawable.icn_delete);
            } else if (items[position].equals("Favorites")) {
                if (!fileChecker) {
                    txtOptions.setText("Add to Favorite");
                    imgOptions.setImageResource(R.drawable.icon_blank_favorite);
                } else {
                    txtOptions.setText("Remove from Favorite");
                    imgOptions.setImageResource(R.drawable.icon_favorite);
                }
            }

            return view;
        }
    }


}