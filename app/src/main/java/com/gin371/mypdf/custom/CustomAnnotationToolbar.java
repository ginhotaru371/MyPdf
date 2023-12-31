package com.gin371.mypdf.custom;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gin371.mypdf.PDFActivity;
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment2;

/**
 * Delegate class that adds a custom annotation toolbar to a PdfViewCtrlTabHostFragment. This sample
 * re-arranges items in the annotation toolbar grouping and manually changes the precedence toolbar.
 */
public class CustomAnnotationToolbar extends CustomizationDelegate {

    public CustomAnnotationToolbar(@NonNull Context context, @NonNull PdfViewCtrlTabHostFragment2 tabHostFragment) {
        super(context, tabHostFragment);
    }

    @Override
    public void applyCustomization(@NonNull PdfViewCtrlTabHostFragment2 tabHostFragment2) {
        // When document loaded, launch the shapes toolbar
        tabHostFragment2.openToolbarWithTag(PDFActivity.NOTES_TOOLBAR_TAG);
    }
}
