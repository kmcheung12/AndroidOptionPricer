package com.comp7405.optionpricer;

import android.widget.TextView;

public class EditTextDoubleValidator extends EditTextValidator {

    public EditTextDoubleValidator(TextView textView) {
        super(textView);
    }

    @Override
    public void validate(TextView textView, String text) {
        try {
          Double.parseDouble(text);
        } catch (NumberFormatException e) {
            textView.setError("Number");
        }
    }
}
