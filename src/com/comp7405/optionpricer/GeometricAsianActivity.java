package com.comp7405.optionpricer;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class GeometricAsianActivity extends Activity implements  OnClickListener, OnCheckedChangeListener  {
	private Button bCalculate;
	private EditText etStockPrice, etStrikePrice, etTimetoMaturity, etSigma, etInterestRate,etObservation;
	private RadioGroup rgOptionType;
	private TextView tvResult;
	
	private OptionType optionType;
	private double S,K,T, sigma,r,n;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geoasianoption);
		
		initialize();
		
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch(checkedId){
		
		case R.id.rbCall:
			optionType = OptionType.CALL;
			break;
		case R.id.rbPut:
			optionType = OptionType.PUT;
			break;
		}
		
	}
	
	@Override
	public void onClick(View arg0) {
        try {
            Button button = (Button)arg0;
            button.setError(null);

            OptionPricer pricer = new OptionPricer();

            S = Double.parseDouble(etStockPrice.getText().toString());
            K = Double.parseDouble(etStrikePrice.getText().toString());
            T = Double.parseDouble(etTimetoMaturity.getText().toString());
            sigma = Double.parseDouble(etSigma.getText().toString());
            r = Double.parseDouble(etInterestRate.getText().toString());
            n = Double.parseDouble(etObservation.getText().toString());

            double result = pricer.asianGeometric(optionType, S, K, T, sigma, r, n);
            String msg = String.format("Option price: %.4f", result);
            tvResult.setText(msg);
        } catch (Exception e) {
            Button button = (Button)arg0;
            button.setError("Invalid input");
            e.printStackTrace();
        }
	}
	
	public void initialize(){
		bCalculate = (Button) findViewById(R.id.bCalculate);
		bCalculate.setOnClickListener(this);

		etStockPrice = (EditText) findViewById(R.id.etStockPrice);
        new EditTextDoubleValidator(etStockPrice);

		etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
        new EditTextDoubleValidator(etStrikePrice);

		etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity);
        new EditTextDoubleValidator(etTimetoMaturity);

		etSigma= (EditText) findViewById(R.id.etSigma);
        new EditTextFractionValidator(etSigma);

		etInterestRate= (EditText) findViewById(R.id.etInterestRate);
        new EditTextFractionValidator(etInterestRate);

        etObservation = (EditText) findViewById(R.id.etObservation);
        new EditTextDoubleValidator(etObservation);

        tvResult = (TextView) findViewById(R.id.tvResult);
        rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
        rgOptionType.setOnCheckedChangeListener(this);
        onCheckedChanged(rgOptionType, rgOptionType.getCheckedRadioButtonId());
	}

}
