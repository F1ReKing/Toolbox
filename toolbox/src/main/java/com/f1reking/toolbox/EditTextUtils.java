package com.f1reking.toolbox;

import android.text.Selection;
import android.text.Spannable;
import android.widget.EditText;

/**
 * 控件辅助工具类
 * Created by F1ReKing on 2016/1/2.
 */
public class EditTextUtils {

    private EditTextUtils() {
        throw new Error("Do not need instantiate!");
    }

    public static void setEditTextCursorLocation(EditText editText) {
        CharSequence text = editText.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
    }
}
