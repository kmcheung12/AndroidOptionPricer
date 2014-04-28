package com.comp7405.optionpricer;

import java.util.Random;

import android.util.Log;

import static com.comp7405.optionpricer.StatisticHelper.*;

enum PricerMethod {STANDARD, CONTROL_VARIATE, ADJUSTED_STRIKE};

public class OptionPricer {
	private Random randomGenerator = new Random();

	public double europeanOptions(double Option, double S, double K, double T, double t, double sigma, double r){
		
		double dt = T -t;
		double d1 = (Math.log(S/K)+(r+Math.pow(sigma, 2)/2)*dt)/(sigma*Math.sqrt(dt));
		double d2 = d1 - sigma*Math.sqrt(dt);
		double Price = -1*Option*K*Math.exp(-r*dt)*CNDF(d2*Option) + Option* S*CNDF(d1 *Option);
	
		return Price;
		
	}
	
	public static double asianGeometric(double Option, double S, double K, double T, double sigma, double r, double n){
			  double sigmaHat = sigma * (Math.sqrt((n+1)*(2*n+1)/(6*n*n)));
			  double muHat = (r - 0.5 * Math.pow(sigma,2)) * (n+1)/(2*n) + 0.5* Math.pow(sigmaHat,2);
			  double d1Hat    = (Math.log(S/K) + (muHat + 0.5* Math.pow(sigmaHat, 2))*T)/ (sigmaHat*Math.sqrt(T));
			  double d2Hat    = d1Hat - sigmaHat*Math.sqrt(T);
			  double N1       = CNDF(Option*d1Hat);
			  double N2       = CNDF(Option*d2Hat);
			  double price    = Math.exp(-r*T) * (Option * S * Math.exp(muHat*T) * N1 - Option*K* N2);
			return price;
	}

	public double[] asianArithmetic(double Option, double S, double K, double T, double sigma, double r, int n, int path, PricerMethod method){
		double[] stockPath = new double[n];
		double[] aPayoff = new double[path];
		double[] gPayoff = new double[path];
	
		double dt = T/n;
		double drift = Math.exp((r-0.5*Math.pow(sigma,2))*dt);
        double growth = 0;

        double sigmaHat = sigma * (Math.sqrt((n + 1) * (2 * n + 1) / ((float)6 * n * n)));
        double muHat = (r - 0.5 * sigma * sigma) * (n + 1) / (2 * n) + 0.5 * sigmaHat * sigmaHat;

        double eAsianA = 0;
        //E_AsianA = S* sum(exp([1:n]*dt*r))/n;
        for (int i =1; i<= n; i++) {
            eAsianA += Math.exp(i*dt*r);
        }
        eAsianA = eAsianA*S/n;
        double eAsianG = S * Math.exp(muHat * T);

        double adjK = K;
		for ( int i = 0; i<path; i++){
			  
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

		    if (method == PricerMethod.ADJUSTED_STRIKE){
                adjK = K + eAsianG - eAsianA;
		    } else {
                adjK = K;
            }
            aPayoff[i] = Math.exp(-r*T)* Math.max(aMean - K,0);
            gPayoff[i] = Math.exp(-r*T)* Math.max(gMean - adjK,0);
        }
		  
        if (method == PricerMethod.STANDARD){

		    return confidenceInterval(aPayoff);
		    
        } else if (method == PricerMethod.CONTROL_VARIATE || method == PricerMethod.ADJUSTED_STRIKE){
		    
		    double covXY = getCovar(aPayoff,gPayoff);
		    double theta = covXY/getVariance(gPayoff);
		    double geo = asianGeometric(Option, S, adjK, T, sigma, r, n);
		    double[] Z = ControlVariateList(aPayoff, theta,geo,gPayoff);
		    return confidenceInterval(Z);
		  }
		return null;
	
	}
	
	public double basketGeometric(double Option, double[] spots, double K, double T, double[] sigmas, double r, double rho){
		double n = spots.length;
		double S1 = spots[0];
		double S2 = spots[1];
		double Sigma1 = sigmas[0];
		double Sigma2 = sigmas[1];
		double SigmaB = Math.sqrt(rho*(Math.pow(Sigma1, 2)+2*Sigma1*Sigma2+Math.pow(Sigma2, 2)))/n;
		double muB = r - 0.5*(Math.pow(Sigma1, 2)+Math.pow(Sigma2, 2))/n +0.5*Math.pow(SigmaB,2);
		double Bg0 = geometricMean(spots);
		double d1Hat = (Math.log(Bg0/K) + (muB + 0.5 * Math.pow(SigmaB,2))*T)/(SigmaB*Math.sqrt(T));
		double d2Hat = d1Hat - SigmaB*Math.sqrt(T);
		double N1 = CNDF(Option*d1Hat);
		double N2 = CNDF(Option*d2Hat);
		double price = Math.exp(-r*T) * (Option*Bg0*Math.exp(muB*T)*N1- Option*K*N2);
		
		return price;
}
	
	public double[] basketArithmetic(double Option, double[] spots, double K, double T, double[] sigmas, double r, double rho, int path, PricerMethod method){
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

        double sigmaB   = Math.sqrt(sigmas[1] * sigmas[1] + rho * 2 * sigmas[1] * sigmas[2] + sigmas[2] * sigmas[2]) / N;
        double muB      = r - 0.5 * (sigmas[1] * sigmas[1] + sigmas[2] * sigmas[2]) / N + 0.5 * sigmaB * sigmaB;
        double forwardG = Math.exp(muB * T);
        double forwardA = Math.exp(r * T);
        double discount  = Math.exp(-r*T);

        double eBasketG = 0; //for adjusted strike
        double eBasketA = 0; //for adjusted strike

        for (int i=0; i<N; i++) {
            double spot = spots[i];
            eBasketG += Math.log(spot);
            eBasketA += spot*forwardA;
        }
        eBasketG = Math.exp(eBasketG / N) *forwardG;
        eBasketA = eBasketA/N;

        double x = 0;
        double z = 0;

        double adjK = K;
        for (int i =0; i< path; i++) {
            x = randomGenerator.nextGaussian();
            z = rho * x + randomGenerator.nextGaussian() * Math.sqrt(1 - rho*rho);
            double sqT = Math.sqrt(T);
            growth[0] = drifts[0] * Math.exp(sigmas[0] * sqT * x);
            growth[1] = drifts[1] * Math.exp(sigmas[1] * sqT * z);

            stocks[0] = spots[0] * growth[0];
            stocks[1] = spots[1] * growth[1];

            basketA = arithmeticMean(stocks);
            basketG = geometricMean(stocks);

            if (method == PricerMethod.ADJUSTED_STRIKE) {
                adjK = K + eBasketG - eBasketA;
            } else {
                adjK = K;
            }

            aPayoff[i] = Math.max(basketA - K, 0) * discount;
            gPayoff[i] = Math.max(basketG - adjK, 0) * discount;
        }

        switch (method) {
            case STANDARD:
                result = confidenceInterval(aPayoff);
                break;
            case CONTROL_VARIATE:
            case ADJUSTED_STRIKE:
                double covXY = getCovar(aPayoff,gPayoff);
                double theta = covXY/getVariance(gPayoff);
                double geo = basketGeometric(Option, spots, K, T, sigmas, r, rho);
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
