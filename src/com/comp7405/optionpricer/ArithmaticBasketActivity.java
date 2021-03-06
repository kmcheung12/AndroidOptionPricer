package com.comp7405.optionpricer;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ArithmaticBasketActivity extends Activity implements
        OnClickListener,
        OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener
{
	private Button bCalculate;
	private EditText  etStrikePrice, etTimetoMaturity, etInterestRate,etPath;
    private RadioGroup rgOptionType, rgMCOption;
    private TextView tvResult;

    private EditText[] etStockPrices , etSigmas;
    private EditText[] etRhos;
    private Spinner spinner;

	private OptionType optionType;
    private PricerMethod method;
	private double K,T,r;
    private int path, n;
	private double[] sigmas, spots;
    private double[][] rhos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arithmeticbasket);
		initialize();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getId()){
		case R.id.rgOption:
			switch(checkedId){
				case R.id.rbCall:
					optionType = OptionType.CALL;
					break;
				case R.id.rbPut:
					optionType = OptionType.PUT;
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
        try {
            Button button = (Button) arg0;
            button.setError(null);
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
            path = Integer.parseInt(etPath.getText().toString());


            ArithmaticBasketTask task = new ArithmaticBasketTask(this, optionType, spots, K, T, sigmas, r, rhos, path, method);
            task.execute();
        } catch (Exception e) {
            Button button = (Button) arg0;
            button.setError("Invalid input");
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
            new EditTextDoubleValidator(etStockPrices[i]);

            etSigmas[i] = (EditText) findViewById(sigmaIds[i]);
            new EditTextFractionValidator(etSigmas[i]);
        }

        bCalculate = (Button) findViewById(R.id.bCalculate);
        bCalculate.setOnClickListener(this);

        etStrikePrice= (EditText) findViewById(R.id.etStrikePrice);
        new EditTextDoubleValidator(etStrikePrice);

        etTimetoMaturity= (EditText) findViewById(R.id.etTimetoMaturity);
        new EditTextDoubleValidator(etTimetoMaturity);

        etInterestRate= (EditText) findViewById(R.id.etInterestRate);
        new EditTextFractionValidator(etInterestRate);

        etPath = (EditText) findViewById(R.id.etPath);
        new EditTextDoubleValidator(etPath);

        tvResult = (TextView) findViewById(R.id.tvResult);
        rgOptionType = (RadioGroup) findViewById(R.id.rgOption);
        rgOptionType.setOnCheckedChangeListener(this);
        rgMCOption = (RadioGroup) findViewById(R.id.rgMCOption);
        rgMCOption.setOnCheckedChangeListener(this);

	    onCheckedChanged(rgMCOption, rgMCOption.getCheckedRadioButtonId());
        onCheckedChanged(rgOptionType, rgOptionType.getCheckedRadioButtonId());
	}

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
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


    class ArithmaticBasketTask extends AsyncTask<Void, Integer, double[]> implements SimulationProgessChange{
        private final OptionType optionType;
        private final double[] spots;
        private final double strike;
        private final double timeToMature;
        private final double[] sigmas;
        private final double r;
        private final double[][] rhos;
        private final int path;
        private final PricerMethod method;
        private final Context context;
        private ProgressDialog dialog;
        private Handler mHandler = new Handler();

        public ArithmaticBasketTask(Context context,OptionType optionType, double[] spots, double K, double T, double[] sigmas, double r, double[][] rhos, int path, PricerMethod method) {
            this.context    = context;
            this.optionType = optionType;
            this.spots      = spots;
            this.strike     = K;
            this.timeToMature = T;
            this.sigmas       = sigmas;
            this.r              = r;
            this.rhos           = rhos;
            this.path           = path;
            this.method         = method;

        }

        @Override
        public void onProgessChange(float progress) {
            if (dialog!= null) {
                final int p = (int)(progress*100);
                if (p > 0) {
                    publishProgress(p);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Simulating...");
            dialog.setMessage("Please wait...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.show();
        }

        @Override
        protected double[] doInBackground(Void... voids) {
            try {
                OptionPricer pricer = new OptionPricer();
                pricer.setListener(this);
                return pricer.basketArithmetic(optionType,spots,strike,timeToMature,sigmas,r,rhos,path,method);
            } catch (Exception e) {
                dialog.dismiss();
                return new double[]{-1,0,0};
            }
        }

        @Override
        protected void onPostExecute(double[] result) {
            dialog.setProgress(100);
            dialog.dismiss();
            tvResult = (TextView) findViewById(R.id.tvResult);
            String msg = String.format("Option price: %.4f\n95%% confidence interval: [%.4f , %.4f]", result[0], result[1], result[2]);
            if (result[0] == -1) {
                msg = "Invalid parameters";
                tvResult.setError(msg);
            }
            tvResult.setText(msg);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

    }
}
