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
	Button bCalculate; 
	EditText etStockPrice, etStrikePrice, etTimetoMaturity, etSigma, etInterestRate,etObservation;
	RadioGroup rgOptionType;
	TextView tvResult;
	
	int OptionType;
	double S,K,T,Sigma,r,n;

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
			OptionType = 1;
			break;
		case R.id.rbPut:
			OptionType = -1;
			break;
		}
		
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		OptionPricer PriceCalculator = new OptionPricer();
		
		S = Double.parseDouble(etStockPrice.getText().toString());
		K = Double.parseDouble(etStrikePrice.getText().toString());
		T = Double.parseDouble(etTimetoMaturity.getText().toString());
		Sigma = Double.parseDouble(etSigma.getText().toString());
		r = Double.parseDouble(etInterestRate.getText().toString());
		n = Double.parseDouble(etObservation.getText().toString());
		
		
		tvResult.setText(Double.toString(PriceCalculator.AsianGeometric(OptionType, S, K, T, Sigma, r, n)));
	}
	
	public void initialize(){
		bCalculate = (Button) findViewById(R.id.bCalculate);
		bCalculate.setOnClickListener(this);
		etStockPrice = (EditText) findViewById(R.id.etStockPrice);
		etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
		etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity); 
		etSigma= (EditText) findViewById(R.id.etSigma);
		etInterestRate= (EditText) findViewById(R.id.etInterestRate);
		tvResult = (TextView) findViewById(R.id.tvResult);
		rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
		rgOptionType.setOnCheckedChangeListener(this);
		etObservation = (EditText) findViewById(R.id.etObservation);
		
		
	}

}
