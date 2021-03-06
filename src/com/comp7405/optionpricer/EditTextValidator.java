package com.comp7405.optionpricer;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

/**
 * Created by alancheung on 2/5/14.
 */
public abstract class EditTextValidator implements TextWatcher, View.OnFocusChangeListener{
    private final TextView textView;

    public EditTextValidator(TextView textView) {
        this.textView = textView;
        this.textView.addTextChangedListener(this);
        this.textView.setOnFocusChangeListener(this);
    }

    public abstract void validate(TextView textView, String text);

    @Override
    final public void afterTextChanged(Editable s) {
        String text = textView.getText().toString();
        validate(textView, text);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

    @Override
    public void onFocusChange(View view, boolean b) {
       if (!b) {
           String text = textView.getText().toString();
           validate(textView, text);
       }
    }
}

