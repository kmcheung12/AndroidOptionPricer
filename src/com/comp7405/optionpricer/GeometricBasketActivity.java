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

public class GeometricBasketActivity extends Activity implements  OnClickListener, OnCheckedChangeListener  {
	Button bCalculate; 
	EditText etStock1Price,etStock2Price, etStrikePrice, etTimetoMaturity, etSigma1,etSigma2, etInterestRate,etCovariance;
	RadioGroup rgOptionType;
	TextView tvResult;
	
	int OptionType;
	double K,T,r,Covariance;
	double[] Sigma, Spots;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.geometricbaseket);
		
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
		
		Spots = new double[] {Double.parseDouble(etStock1Price.getText().toString()),Double.parseDouble(etStock2Price.getText().toString())};
		K = Double.parseDouble(etStrikePrice.getText().toString());
		T = Double.parseDouble(etTimetoMaturity.getText().toString());
		Sigma =  new double[]{Double.parseDouble(etSigma1.getText().toString()),Double.parseDouble(etSigma2.getText().toString())};
		r = Double.parseDouble(etInterestRate.getText().toString());
		Covariance = Double.parseDouble(etCovariance.getText().toString());
		
		
		tvResult.setText(Double.toString(PriceCalculator.BasketGeometric(OptionType, Spots, K, T, Sigma, r, Covariance)));
	}
	
	public void initialize(){
		bCalculate = (Button) findViewById(R.id.bCalculate);
		bCalculate.setOnClickListener(this);
		etStock1Price = (EditText) findViewById(R.id.etStock1Price);
		etStock2Price = (EditText) findViewById(R.id.etStock2Price);
		etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
		etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity); 
		etSigma1= (EditText) findViewById(R.id.etSigma1);
		etSigma2= (EditText) findViewById(R.id.etSigma2);
		etInterestRate= (EditText) findViewById(R.id.etInterestRate);
		tvResult = (TextView) findViewById(R.id.tvResult);
		rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
		rgOptionType.setOnCheckedChangeListener(this);
		etCovariance = (EditText) findViewById(R.id.etCovariance);
		
		
		
	}

}