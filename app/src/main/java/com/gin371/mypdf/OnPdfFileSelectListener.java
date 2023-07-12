package com.gin371.mypdf;

import java.io.File;

public interface OnPdfFileSelectListener {
    void onPdfSelected(File file);
    void onPdfLongClick(File file, int position);

}
