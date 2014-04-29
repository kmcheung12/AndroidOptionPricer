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
	private Button bCalculate;
	private EditText etStock1Price,etStock2Price, etStrikePrice, etTimetoMaturity, etSigma1,etSigma2, etInterestRate,etCovariance;
	private RadioGroup rgOptionType;
	private TextView tvResult;
	
	private OptionType optionType;
	private double K,T,r, rho;
	private double[] sigma, spots;

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
            OptionPricer PriceCalculator = new OptionPricer();

            spots = new double[] {Double.parseDouble(etStock1Price.getText().toString()),Double.parseDouble(etStock2Price.getText().toString())};
            K = Double.parseDouble(etStrikePrice.getText().toString());
            T = Double.parseDouble(etTimetoMaturity.getText().toString());
            sigma =  new double[]{Double.parseDouble(etSigma1.getText().toString()),Double.parseDouble(etSigma2.getText().toString())};
            r = Double.parseDouble(etInterestRate.getText().toString());
            rho = Double.parseDouble(etCovariance.getText().toString());


            tvResult.setText(Double.toString(PriceCalculator.basketGeometric(optionType, spots, K, T, sigma, r, rho)));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
