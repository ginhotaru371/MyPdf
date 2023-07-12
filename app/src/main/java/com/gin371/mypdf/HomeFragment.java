package com.gin371.mypdf;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

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

public class HomeFragment extends Fragment implements OnPdfFileSelectListener {

    View view;
    private ArrayList<FilePath> filePaths = new ArrayList<>();
    private ArrayList<FilePath> recentFilePaths = new ArrayList<>();
    private static final String FILE_NAME_FAVORITE = "favorite.json";
    private static final String FILE_NAME_RECENT = "recent.json";
    private RecyclerView List_pdf;
    private List<File> pdfList;
    PDFAdapter pdfAdapter;
    SearchView searchView;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean fileChecker = false;

    String[] items = {"Rename", "Delete", "Favorites"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        List_pdf = (RecyclerView) view.findViewById(R.id.list_pdf);
        List_pdf.setHasFixedSize(true);
        List_pdf.setLayoutManager(new LinearLayoutManager(getContext()));
        List_pdf.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        runtimePermission();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.home_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                runtimePermission();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void runtimePermission() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                displayPDF();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .check();
    }

    public ArrayList<File> findPDF (File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (File singleFile: files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findPDF(singleFile));
                }
                else {
                    if (singleFile.getName().endsWith(".pdf")) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }
        return arrayList;
    }

    private void displayPDF() {

        pdfList = new ArrayList<>();
        pdfList.addAll(findPDF(Environment.getExternalStorageDirectory()));
        pdfAdapter = new PDFAdapter(view.getContext(), pdfList, this);
        List_pdf.setAdapter(pdfAdapter);
    }

    @Override
    public void onPdfSelected(File file) {
        Intent intent = new Intent(view.getContext(), PDFActivity.class);
        intent.putExtra("path", file.getAbsolutePath());
        startActivity(intent);

        checkFileInRecentList(file.getAbsolutePath(), FILE_NAME_RECENT);

        if (!fileChecker) {
            addRecent(file, FILE_NAME_RECENT);
        }
    }

    @Override
    public void onPdfLongClick(File file, int position) {

        checkFileInFavoriteList(file.getAbsolutePath(), FILE_NAME_FAVORITE);

        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select Options");
        ListView options = (ListView) optionDialog.findViewById(R.id.list_option);
        CustomAdapter customAdapter = new CustomAdapter();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void loadCurrentList(String fileName, String dataFileName) {
        FileInputStream fileIn = null;

        if (dataFileName == FILE_NAME_FAVORITE) {
            try {
                fileIn = getActivity().openFileInput(fileName);
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

        } else if (dataFileName == FILE_NAME_RECENT) {
            try {
                fileIn = getActivity().openFileInput(fileName);
                InputStreamReader inputStreamReader = new InputStreamReader(fileIn);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String json;

                while ((json = bufferedReader.readLine()) != null) {
                    stringBuilder.append(json);
                }

                Type type = new TypeToken<ArrayList<FilePath>>(){}.getType();
                Gson gson = new Gson();
                recentFilePaths = gson.fromJson(stringBuilder.toString(), type);

                fileIn.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void checkFileInFavoriteList(String filePath, String dataFile) {
        loadCurrentList(dataFile, dataFile);

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
    }
    private void checkFileInRecentList(String filePath, String dataFile) {
        loadCurrentList(dataFile, dataFile);

        if (recentFilePaths != null) {
            for (int i = recentFilePaths.size() - 1; i >= 0; i--) {
                if (recentFilePaths.get(i).getFilePath().equals(filePath)) {
                    fileChecker = true;
                    break;
                } else {
                    fileChecker = false;
                }
            }
        }
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
            fileOut = getActivity().openFileOutput(FILE_NAME_FAVORITE, getContext().MODE_PRIVATE);
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
            fileOut = getActivity().openFileOutput(FILE_NAME_FAVORITE, getContext().MODE_PRIVATE);
            fileOut.write(json.getBytes());
            fileOut.close();

            Toast.makeText(getContext(), "Removed from Favorite List", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addRecent(File file, String dataFile) {

        FileOutputStream fileOut = null;
        FilePath filePath = new FilePath(file.getAbsolutePath());
        ArrayList<FilePath> newFilePaths = new ArrayList<>();
        newFilePaths.add(filePath);

        if (recentFilePaths != null) {
            recentFilePaths.addAll(newFilePaths);
        } else {
            recentFilePaths = newFilePaths;
        }
        String json = null;

        Gson gson = new Gson();
        json = gson.toJson(recentFilePaths);

        try {
            fileOut = getActivity().openFileOutput(dataFile, getContext().MODE_PRIVATE);
            fileOut.write(json.getBytes());
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.tool_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }

            private void filter(String newText) {
                List<File> filterList= new ArrayList<>();
                for (File file : pdfList) {
                    if (file.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filterList.add(file);
                    }
                }
                pdfAdapter.filterList(filterList);
            }
        });
    }
}