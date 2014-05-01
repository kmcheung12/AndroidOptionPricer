package com.comp7405.optionpricer;

import java.util.Random;

import android.util.Log;

import static com.comp7405.optionpricer.StatisticHelper.*;

enum PricerMethod {STANDARD, CONTROL_VARIATE, ADJUSTED_STRIKE};
enum OptionType {CALL,PUT};

public class OptionPricer {
	private Random randomGenerator = new Random();

    private SimulationProgessChange listener;

    public SimulationProgessChange getListener() {
        return listener;
    }

    public void setListener(SimulationProgessChange listener) {
        this.listener = listener;
    }
    OptionPricer(){
        randomGenerator.setSeed(12345678); // setting fixed seed, for having fixed result
    }

	public double europeanOptions(OptionType optionType, double S, double K, double T, double t, double sigma, double r){
        double option   = optionType == OptionType.CALL ? 1.0 : -1.0;
		double dt       = T -t;
		double d1       = (Math.log(S/K)+(r+Math.pow(sigma, 2)/2)*dt)/(sigma*Math.sqrt(dt));
		double d2       = d1 - sigma*Math.sqrt(dt);
		double Price    = -1*option*K*Math.exp(-r*dt)*CNDF(d2*option) + option* S*CNDF(d1 *option);
	
		return Price;
		
	}
	
	public  double asianGeometric(OptionType optionType, double S, double K, double T, double sigma, double r, double n){
        double option   = optionType == OptionType.CALL ? 1.0 : -1.0;
        double sigmaHat = sigma * (Math.sqrt((n+1)*(2*n+1)/(6*n*n)));
        double muHat    = (r - 0.5 * Math.pow(sigma,2)) * (n+1)/(2*n) + 0.5* Math.pow(sigmaHat,2);
        double d1Hat    = (Math.log(S/K) + (muHat + 0.5* Math.pow(sigmaHat, 2))*T)/ (sigmaHat*Math.sqrt(T));
        double d2Hat    = d1Hat - sigmaHat*Math.sqrt(T);
        double N1       = CNDF(option*d1Hat);
        double N2       = CNDF(option*d2Hat);
        double price    = Math.exp(-r*T) * (option * S * Math.exp(muHat*T) * N1 - option*K* N2);
        return price;
	}

	public double[] asianArithmetic(OptionType optionType , double S, double K, double T, double sigma, double r, int n, int path, PricerMethod method){
        double option       = optionType == OptionType.CALL ? 1.0 : -1.0;
        double[] stockPath  = new double[n];
		double[] aPayoff    = new double[path];
		double[] gPayoff    = new double[path];
	
		double dt           = T/n;
		double drift        = Math.exp((r-0.5*Math.pow(sigma,2))*dt);
        double growth       = 0;

        double sigmaHat     = sigma * (Math.sqrt((n + 1) * (2 * n + 1) / ((float)6 * n * n)));
        double muHat        = (r - 0.5 * sigma * sigma) * (n + 1) / (2 * n) + 0.5 * sigmaHat * sigmaHat;

        double eAsianA      = 0;
        //E_AsianA = S* sum(exp([1:n]*dt*r))/n;
        for (int i =1; i<= n; i++) {
            eAsianA += Math.exp(i*dt*r);
        }
        eAsianA             = eAsianA*S/n;
        double eAsianG      = S * Math.exp(muHat * T);
        double adjK         = K;
        if (method == PricerMethod.ADJUSTED_STRIKE){
            adjK = K + eAsianG - eAsianA;
        }
        for ( int i = 0; i<path; i++){
            if (i%(path/20) == 0) {
                if (listener != null) {
                    listener.onProgessChange(i/path);
                }
            }
            for (int j = 0; j<n; j++){
                growth = drift * Math.exp(sigma*Math.sqrt(dt)*randomGenerator.nextGaussian());

                if (j == 0){
                  stockPath [0] = S * growth;
              }else{
                  stockPath[j] = stockPath[j-1] * growth;
              }

            }

            double aMean = arithmeticMean(stockPath);
            double gMean = geometricMean(stockPath);

            aPayoff[i] = Math.exp(-r*T)* Math.max(option * (aMean - K),0);
            gPayoff[i] = Math.exp(-r*T)* Math.max(option * (gMean - adjK),0);
        }
		  
        if (method == PricerMethod.STANDARD){

		    return confidenceInterval(aPayoff);
		    
        } else if (method == PricerMethod.CONTROL_VARIATE || method == PricerMethod.ADJUSTED_STRIKE){
		    
		    double covXY = getCovar(aPayoff,gPayoff);
		    double theta = covXY/getVariance(gPayoff);
		    double geo = asianGeometric(optionType, S, adjK, T, sigma, r, n);
		    double[] Z = ControlVariateList(aPayoff, theta,geo,gPayoff);
		    return confidenceInterval(Z);
		  }
		return null;
	
	}

    public double geometricBasketVolatility(double[] sigmas, double[][] rhos) {
        int n = sigmas.length;
        double sum = 0;
        for (int i =0; i< n; i++) {
            for (int j = 0; j<n; j++) {
                sum += rhos[i][j] * sigmas[i] * sigmas[j];
            }
        }
        double sigmaB = Math.sqrt(sum)/n;
        return sigmaB;
    }

    public double geometricBasketDrift(double[] sigmas, double sigmaB, double r){

        int n = sigmas.length;
        double sum = 0;
        for (int i =0; i< n; i++) {
            sum +=sigmas[i] * sigmas[i];
        }
        double muB      = r - 0.5 * (sum) / n + 0.5 * sigmaB * sigmaB;
        return muB;
    }


	public double basketGeometric(OptionType optionType, double[] spots, double K, double T, double[] sigmas, double r, double[][] rhos){
		double option   = optionType == OptionType.CALL?1.0:-1.0;

        double sigmaB   = geometricBasketVolatility(sigmas, rhos);
		double muB      = geometricBasketDrift(sigmas,sigmaB,r);

		double Bg0      = geometricMean(spots);
		double d1Hat    = (Math.log(Bg0/K) + (muB + 0.5 * Math.pow(sigmaB,2))*T)/(sigmaB*Math.sqrt(T));
		double d2Hat    = d1Hat - sigmaB*Math.sqrt(T);
		double N1       = CNDF(option*d1Hat);
		double N2       = CNDF(option*d2Hat);
		double price    = Math.exp(-r*T) * (option*Bg0*Math.exp(muB*T)*N1- option*K*N2);
		
		return price;
}
	
	public double[] basketArithmetic(OptionType optionType, double[] spots, double K, double T, double[] sigmas, double r, double[][] rhos, int path, PricerMethod method){
        double option   = optionType == OptionType.CALL? 1.0:-1.0;
        int N           = spots.length; //no. of assets
		double[] growth = new double[N];
		double[] stocks = new double[N];
		double basketA  = 0;
		double basketG  = 0;
		double[] aPayoff =new double[path];
		double[] gPayoff =new double[path];

        double[] result = null;

        double[] drifts  = new double[N];
        for (int i =0; i< N; i++) {
            drifts[i] = Math.exp((r-0.5*sigmas[i]*sigmas[i])*T);
        }

        double sigmaB   = geometricBasketVolatility(sigmas, rhos);
        double muB      = geometricBasketDrift(sigmas, sigmaB, r);

        double forwardG = Math.exp(muB * T);
        double forwardA = Math.exp(r * T);
        double discount  = Math.exp(-r*T);

        double eBasketG = 0; //for adjusted strike
        double eBasketA = 0; //for adjusted strike

        for (int i=0; i<N; i++) {
            double spot = spots[i];
            eBasketG += Math.log(spot);
            eBasketA += spot *forwardA;
        }
        eBasketG = Math.exp(eBasketG / N) *forwardG;
        eBasketA = eBasketA/N;

        double[][] lowerTriMatrix = chol(rhos);
        double[][] upperTriMatrix = transpose(lowerTriMatrix);
        double[][] uncorrSamples = new double[1][N];
        double[][] corrSamples = new double[1][N];

        double adjK = K;
        if (method == PricerMethod.ADJUSTED_STRIKE) {
                adjK = K + eBasketG - eBasketA;
        }

        for (int i =0; i< path; i++) {

            if (i%(path/20) == 0) {
                if (listener != null) {
                    listener.onProgessChange((float)i/path);
                }
            }
            for (int j = 0; j <N ; j++) {
                uncorrSamples[0][j] = randomGenerator.nextGaussian();
            }

            try {
                corrSamples = matrixMul(uncorrSamples, upperTriMatrix);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            double sqT = Math.sqrt(T);
            for (int j = 0; j<N; j++) {
                growth[j] = drifts[j] * Math.exp(sigmas[j] * sqT * corrSamples[0][j]);
                stocks[j] = spots[j] * growth[j];
            }


            basketA = arithmeticMean(stocks);
            basketG = geometricMean(stocks);



            aPayoff[i] = Math.max(option * (basketA - K) *discount, 0) ;
            gPayoff[i] = Math.max(option * (basketG - adjK) *discount, 0) ;
        }

        switch (method) {
            case STANDARD:
                result = confidenceInterval(aPayoff);
                break;
            case CONTROL_VARIATE:
            case ADJUSTED_STRIKE:
                double covXY = getCovar(aPayoff,gPayoff);
                double theta = covXY/getVariance(gPayoff);
                double geo = basketGeometric(optionType, spots, adjK, T, sigmas, r, rhos);
                double[] Z = ControlVariateList(aPayoff, theta,geo,gPayoff);
                result = confidenceInterval(Z);
            default:
                break;
        }
		return result;
		
	}

	
	//Debug output in android
	
	public final static class Debug{
	    private Debug (){}

	    public static void out (Object msg){
	        Log.i ("info", msg.toString ());
	    }
	}
}
