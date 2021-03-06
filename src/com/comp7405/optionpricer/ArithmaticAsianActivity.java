package com.comp7405.optionpricer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
	
	private OptionType optionType;
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
            S = Double.parseDouble(etStockPrice.getText().toString());
            K = Double.parseDouble(etStrikePrice.getText().toString());
            T = Double.parseDouble(etTimetoMaturity.getText().toString());
            Sigma = Double.parseDouble(etSigma.getText().toString());
            r = Double.parseDouble(etInterestRate.getText().toString());
            n = Integer.parseInt(etObservation.getText().toString());
            path = Integer.parseInt(etPath.getText().toString());
            ArithmaticAsianTask task = new ArithmaticAsianTask(this,optionType, S, K, T, Sigma, r, n, path, method);
            task.execute();

        } catch (Exception e) {
            Button button = (Button) arg0;
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
    class ArithmaticAsianTask extends AsyncTask<Void, Integer, double[]> implements SimulationProgessChange{
        private final OptionType optionType;
        private final double strike;
        private final double timeToMature;
        private final double r;
        private final int path;
        private final PricerMethod method;
        private final Context context;
        private final double spot;
        private final double sigma;
        private final int n;
        private ProgressDialog dialog;

        public ArithmaticAsianTask(Context context,OptionType optionType, double spot, double K, double T, double sigma, double r, int n, int path, PricerMethod method) {
            this.context    = context;
            this.optionType = optionType;
            this.spot       = spot;
            this.strike     = K;
            this.timeToMature = T;
            this.sigma          = sigma;
            this.r              = r;
            this.n              = n;
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
                return pricer.asianArithmetic(optionType, spot, strike, timeToMature, sigma, r, n, path, method);
            } catch (Exception e) {
                dialog.dismiss();
                return new double[]{-1,0,0};
            }
        }

        @Override
        protected void onPostExecute(double[] result) {
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
