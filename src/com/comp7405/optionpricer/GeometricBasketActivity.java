package com.comp7405.optionpricer;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class GeometricBasketActivity extends Activity implements  OnClickListener, OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
	private Button bCalculate;
	private EditText etStrikePrice, etTimetoMaturity, etInterestRate;
	private RadioGroup rgOptionType;
	private TextView tvResult;

    private EditText[] etStockPrices , etSigmas;
    private EditText[] etRhos;
    private Spinner spinner;

	private OptionType optionType;
	private double K,T,r;
	private double[] sigmas, spots;
    private double[][] rhos;
    private int n;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
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

            spots = new double[n];
            sigmas = new double[n];
            for (int i=0; i<n; i++) {
                spots[i] = Double.parseDouble(etStockPrices[i].getText().toString());
                sigmas[i] = Double.parseDouble(etSigmas[i].getText().toString());
            }


            rhos = new double[n][n];

            int count = 0;
            for (int i =0; i<n; i++) {
                for (int j =0; j<= i; j++) {
                    rhos[i][j] = Double.parseDouble(etRhos[count].getText().toString());
                    rhos[j][i] = rhos[i][j];
                    count +=1;
                }
            }

            K = Double.parseDouble(etStrikePrice.getText().toString());
            T = Double.parseDouble(etTimetoMaturity.getText().toString());
            r = Double.parseDouble(etInterestRate.getText().toString());
            double result = PriceCalculator.basketGeometric(optionType, spots, K, T, sigmas, r, rhos);
            String msg = String.format("Option price: %.4f", result);
            tvResult.setText(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void initialize(){
        spinner = (Spinner) findViewById(R.id.sp_number_of_stock);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.number_of_stocks,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        int[] spotIds   = {R.id.etStock1Price, R.id.etStock2Price, R.id.etStock3Price, R.id.etStock4Price, R.id.etStock5Price};
        int[] sigmaIds  = {R.id.etSigma1, R.id.etSigma2, R.id.etSigma3, R.id.etSigma4, R.id.etSigma5};

        etStockPrices = new EditText[spotIds.length];
        etSigmas      = new EditText[spotIds.length];

        int[] rhoIds    = {
                R.id.rho_1_1,
                R.id.rho_2_1,R.id.rho_2_2,
                R.id.rho_3_1,R.id.rho_3_2,R.id.rho_3_3,
                R.id.rho_4_1,R.id.rho_4_2,R.id.rho_4_3,R.id.rho_4_4,
                R.id.rho_5_1,R.id.rho_5_2,R.id.rho_5_3,R.id.rho_5_4,R.id.rho_5_5};
        etRhos        = new EditText[rhoIds.length];
        for (int i=0; i<rhoIds.length; i++) {
            etRhos[i] = (EditText) findViewById(rhoIds[i]);
            etRhos[i].addTextChangedListener(new EditTextFractionValidator(etRhos[i]));
        }

        for (int i=0; i<spotIds.length; i++) {
            etStockPrices[i] = (EditText) findViewById(spotIds[i]);
            etSigmas[i] = (EditText) findViewById(sigmaIds[i]);

            etStockPrices[i].addTextChangedListener(new EditTextDoubleValidator(etStockPrices[i]));
            etSigmas[i].addTextChangedListener(new EditTextDoubleValidator(etSigmas[i]));
        }

        bCalculate = (Button) findViewById(R.id.bCalculate);
		bCalculate.setOnClickListener(this);

		etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
        etStrikePrice.addTextChangedListener(new EditTextDoubleValidator(etStrikePrice));

		etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity);
        etTimetoMaturity.addTextChangedListener(new EditTextDoubleValidator(etTimetoMaturity));

		etInterestRate= (EditText) findViewById(R.id.etInterestRate);
        etInterestRate.addTextChangedListener(new EditTextFractionValidator(etInterestRate));

		tvResult = (TextView) findViewById(R.id.tvResult);
		rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
		rgOptionType.setOnCheckedChangeListener(this);

	    onCheckedChanged(rgOptionType,rgOptionType.getCheckedRadioButtonId());
	}

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

        n = Integer.parseInt(adapterView.getItemAtPosition(pos).toString());
        int[] linearLayoutIds = {R.id.ll_stock_price1, R.id.ll_stock_price2, R.id.ll_stock_price3, R.id.ll_stock_price4, R.id.ll_stock_price5};
        int[] tableRowIds = {R.id.table_row1, R.id.table_row2, R.id.table_row3, R.id.table_row4, R.id.table_row5};
        int[] tableLabelIds = {R.id.table_label_1,R.id.table_label_2,R.id.table_label_3,R.id.table_label_4,R.id.table_label_5};

        for (int i=0; i< linearLayoutIds.length; i++) {
            LinearLayout ll = (LinearLayout) findViewById(linearLayoutIds[i]);
            TableRow tr = (TableRow) findViewById(tableRowIds[i]);
            TextView tv = (TextView) findViewById(tableLabelIds[i]);
            if (i < n) {
                if (ll.getVisibility()!=View.VISIBLE) {
                    ll.setVisibility(View.VISIBLE);
                }

                if (tr.getVisibility()!=View.VISIBLE) {
                    tr.setVisibility(View.VISIBLE);
                }

                if (tv.getVisibility()!=View.VISIBLE) {
                    tv.setVisibility(View.VISIBLE);
                }

            } else {
                if (ll.getVisibility()==View.VISIBLE) {
                    ll.setVisibility(View.GONE);
                }
                if (tr.getVisibility()==View.VISIBLE) {
                    tr.setVisibility(View.GONE);
                }
                if (tv.getVisibility()==View.VISIBLE) {
                    tv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
