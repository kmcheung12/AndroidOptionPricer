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

public class EuorpeanOptionActivity extends Activity implements  OnClickListener, OnCheckedChangeListener  {
	Button bCalculate; 
	EditText etStockPrice, etStrikePrice, etTimetoMaturity, etSigma, etInterestRate;
	RadioGroup rgOptionType;
	TextView tvResult;
	
	OptionType optionType;
	double S,K,T,Sigma,r;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.europeanoption);
		
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

            OptionPricer pricer = new OptionPricer();

            S = Double.parseDouble(etStockPrice.getText().toString());
            K = Double.parseDouble(etStrikePrice.getText().toString());
            T = Double.parseDouble(etTimetoMaturity.getText().toString());
            Sigma = Double.parseDouble(etSigma.getText().toString());
            r = Double.parseDouble(etInterestRate.getText().toString());

            double result = pricer.europeanOptions(optionType, S, K, T, 0, Sigma, r);
            String msg = String.format("Option price: %.4f", result);
            tvResult.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void initialize(){
		bCalculate = (Button) findViewById(R.id.bCalculate);
		bCalculate.setOnClickListener(this);

		etStockPrice = (EditText) findViewById(R.id.etStockPrice);
        etStockPrice.addTextChangedListener(new EditTextDoubleValidator(etStockPrice ));

		etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
        etStrikePrice.addTextChangedListener(new EditTextDoubleValidator(etStrikePrice));

		etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity);
        etTimetoMaturity.addTextChangedListener(new EditTextDoubleValidator(etTimetoMaturity));

		etSigma= (EditText) findViewById(R.id.etSigma);
        etSigma.addTextChangedListener(new EditTextFractionValidator(etSigma));

		etInterestRate= (EditText) findViewById(R.id.etInterestRate);
        etInterestRate.addTextChangedListener(new EditTextFractionValidator(etInterestRate));

		tvResult = (TextView) findViewById(R.id.tvResult);
		rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
		rgOptionType.setOnCheckedChangeListener(this);
		onCheckedChanged(rgOptionType, rgOptionType.getCheckedRadioButtonId());
	}

}
