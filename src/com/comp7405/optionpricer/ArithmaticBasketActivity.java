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

public class ArithmaticBasketActivity extends Activity implements  OnClickListener, OnCheckedChangeListener  {
	Button bCalculate; 
	EditText etStock1Price,etStock2Price, etStrikePrice, etTimetoMaturity, etSigma1,etSigma2, etInterestRate,etCovariance,etPath;
	RadioGroup rgOptionType, rgMCOption;
	TextView tvResult;
	
	int OptionType, Method;
	double K,T,r,Covariance,path;
	double[] Sigma, Spots;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arithmeticbasket);
		
		initialize();
		
		
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		switch (group.getId()){
		case R.id.rgOption:
			switch(checkedId){
				case R.id.rbCall:
					OptionType = 1;
					break;
				case R.id.rbPut:
					OptionType = -1;
					break;}
			break;
		case R.id.rgMCOption:
			switch(checkedId){
				case R.id.rbStandardMC:
					Method = 0;
					break;
				case R.id.rbControlVariate:
					Method = 1;
					break;
				case R.id.rbControlVariateWithStrike:
					Method = 2;
					break;
				}
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
		path = Double.parseDouble(etPath.getText().toString());
		
		
		double[] Result = PriceCalculator.ArithmeticBasket(OptionType, Spots, K, T, Sigma, r, Covariance, (int) path, Method);
		tvResult.setText("Option Price is " + Double.toString(Result[0]) + " With 95% confidence interval at " +Double.toString(Result[1]) + " and "  +Double.toString(Result[2]) );

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
		etPath = (EditText) findViewById(R.id.etPath);
		rgMCOption = (RadioGroup) findViewById(R.id.rgMCOption);
		rgMCOption.setOnCheckedChangeListener(this);
		
		
	}

}