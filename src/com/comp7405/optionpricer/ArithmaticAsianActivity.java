package com.comp7405.optionpricer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ArithmaticAsianActivity extends Activity implements  OnClickListener, OnCheckedChangeListener  {
	private Button bCalculate;
	private EditText etStockPrice, etStrikePrice, etTimetoMaturity, etSigma, etInterestRate,etObservation, etPath;
	private RadioGroup rgOptionType, rgMCOption;
	private TextView tvResult;
	
	private int OptionType;
    private PricerMethod method;
	private double S,K,T,Sigma,r;
    private int n, path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arithmaticasian);
		
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
					method = PricerMethod.STANDARD;
					break;
				case R.id.rbControlVariate:
					method = PricerMethod.CONTROL_VARIATE;
					break;
				case R.id.rbControlVariateWithStrike:
					method = PricerMethod.ADJUSTED_STRIKE;
					break;
				}
			break;
		}
	}
	
	@Override
	public void onClick(View arg0) {
		OptionPricer PriceCalculator = new OptionPricer();
		
		S = Double.parseDouble(etStockPrice.getText().toString());
		K = Double.parseDouble(etStrikePrice.getText().toString());
		T = Double.parseDouble(etTimetoMaturity.getText().toString());
		Sigma = Double.parseDouble(etSigma.getText().toString());
		r = Double.parseDouble(etInterestRate.getText().toString());
		n = Integer.parseInt(etObservation.getText().toString());
		path = Integer.parseInt(etPath.getText().toString());
		
		double[] Result = PriceCalculator.asianArithmetic(OptionType, S, K, T, Sigma, r, n, path, method);
		
		//tvResult.setText(Integer.toString(method)+ Integer.toString(OptionType));
		
		tvResult.setText("Option Price is " + Double.toString(Result[0]) + " With 95% confidence interval at " +Double.toString(Result[1]) + " and "  +Double.toString(Result[2]) );
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
		etPath = (EditText) findViewById(R.id.etPath);
		rgMCOption = (RadioGroup) findViewById(R.id.rgMCOption);
		rgMCOption.setOnCheckedChangeListener(this);
		
	}
}
