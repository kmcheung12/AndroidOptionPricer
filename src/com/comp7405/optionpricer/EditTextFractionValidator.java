package com.comp7405.optionpricer;

import android.widget.TextView;

/**
 * Created by alancheung on 2/5/14.
 */


public class EditTextFractionValidator extends EditTextValidator{

    public EditTextFractionValidator(TextView textView) {
        super(textView);
    }

    @Override
    public void validate(TextView textView, String text) {
        try {
            double val = Double.parseDouble(text);
            if (val < 0) {
                textView.setError(">0");
            }
            if (val > 1) {
                textView.setError("<1");
            }
        } catch (NumberFormatException e) {
            textView.setError("0-1");

        }
    }
}
