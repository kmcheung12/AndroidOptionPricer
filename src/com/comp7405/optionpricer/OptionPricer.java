package com.comp7405.optionpricer;

import java.util.Random;

import android.util.Log;

public class OptionPricer {
	Random randomGenerator = new Random();
	
	public double EuropeanOptions(double Option, double S, double K, double T, double t, double sigma, double r){
		
		double dt = T -t;
		double d1 = (Math.log(S/K)+(r+Math.pow(sigma, 2)/2)*dt)/(sigma*Math.sqrt(dt));
		double d2 = d1 - sigma*Math.sqrt(dt);
		double Price = -1*Option*K*Math.exp(-r*dt)*CNDF(d2*Option) + Option* S*CNDF(d1 *Option);
	
		return Price;
		
	}
	
	public static double AsianGeometric (double Option, double S, double K, double T, double sigma, double r, double n){
			  double sigmaHat = sigma * (Math.sqrt((n+1)*(2*n+1)/(6*n*n)));
			  double muHat = (r - 0.5 * Math.pow(sigma,2)) * (n+1)/(2*n) + 0.5* Math.pow(sigmaHat,2);
			  double d1Hat    = (Math.log(S/K) + (muHat + 0.5* Math.pow(sigmaHat, 2))*T)/ (sigmaHat*Math.sqrt(T));
			  double d2Hat    = d1Hat - sigmaHat*Math.sqrt(T);
			  double N1       = CNDF(Option*d1Hat);
			  double N2       = CNDF(Option*d2Hat);
			  double price    = Math.exp(-r*T) * (Option * S * Math.exp(muHat*T) * N1 - Option*K* N2);
			return price;
	}
	//Asian Arithmetic Monte Carlo, Option 0 = standard, 1 = Variate control 2 = variate control with adj K
	public double[] AsianArithmetic(double Option, double S, double K, double T, double sigma, double r, double n, double path, double method){
		double[] stockPath = new double[(int) n];
		double[] aPayoff = new double[(int) path];
		double[] gPayoff = new double[(int) path];
	
		double dt = T/n;
	    
		double drift = Math.exp((r-0.5*Math.pow(sigma,2))*dt);
		
		  for ( int i = 0; i<path; i++){
			  
		    for (int j = 0; j<n; j++){
		      double growth = drift * Math.exp(sigma*Math.sqrt(dt)*randomGenerator.nextGaussian());
		      
		      if (j == 0){
		        stockPath [0] = S * growth;
		      }else{
		        stockPath[j] = stockPath[j-1] * growth;
		      }
		     
		    }
		    
		    double aMean = average(stockPath);
		    double gMean = GeoMean(stockPath);
		    
		    if (method == 2){
		      double adjK = K + gMean - aMean;
		      aPayoff[i] = Math.exp(-r*T)* Math.max(aMean -adjK,0);
		      gPayoff[i] = Math.exp(-r*T)* Math.max(gMean-adjK,0);
		    }else{	    
		     aPayoff[i] = Math.exp(-r*T)*Math.max(Option*aMean -Option*K,0);
		     gPayoff[i] = Math.exp(-r*T)*Math.max(Option*gMean-Option*K,0);
		    }
		  }
		  
		  if (method == 0){
		
		    double Pmean = average(aPayoff);
		    double Pstd = getStdDev(aPayoff);
		    double Uconf= Pmean-1.96*Pstd/Math.sqrt(path);
		    double Lconf = Pmean+1.96*Pstd/Math.sqrt(path);
		    double[] result = {Pmean, Uconf,Lconf};
		    
		    return result;
		    
		  }else if (method == 1 || method == 2){
		    
		    double covXY = getCovar(aPayoff,gPayoff);
		    double theta = covXY/getVariance(gPayoff);
		    Debug.out(theta);
		    Debug.out(covXY);
		    double geo = AsianGeometric(Option, S, K, T, sigma, r, n);
		    double[] Z = ControlVariateList(aPayoff, theta,geo,gPayoff);
		    double Zmean = average(Z);
		    double Zstd = getStdDev(Z);
		    double Uconf = Zmean-1.96*Zstd/Math.sqrt(path);
		    double Lconf = Zmean+1.96*Zstd/Math.sqrt(path);
		    
		    double[] result = {Zmean, Uconf,Lconf};
		    
		    return result;
		  }
		return null;
	
	}
	
	public double BasketGeometric (double Option, double[] Spots, double K, double T, double[] sigma, double r, double rho){
		double n = Spots.length;
		double S1 = Spots[0];
		double S2 = Spots[1];
		double Sigma1 = sigma[0];
		double Sigma2 = sigma[1];
		double SigmaB = Math.sqrt(rho*(Math.pow(Sigma1, 2)+2*Sigma1*Sigma2+Math.pow(Sigma2, 2)))/n;
		double muB = r - 0.5*(Math.pow(Sigma1, 2)+Math.pow(Sigma2, 2))/n +0.5*Math.pow(SigmaB,2);
		double Bg0 = GeoMean(Spots);
		double d1Hat = (Math.log(Bg0/K) + (muB + 0.5 * Math.pow(SigmaB,2))*T)/(SigmaB*Math.sqrt(T));
		double d2Hat = d1Hat - SigmaB*Math.sqrt(T);
		double N1 = CNDF(Option*d1Hat);
		double N2 = CNDF(Option*d2Hat);
		double price = Math.exp(-r*T) * (Option*Bg0*Math.exp(muB*T)*N1- Option*K*N2);
		
		return price;
}
	
	public double[] ArithmeticBasket (double Option, double[] Spots, double K, double T, double[] sigma, double r, double rho, int path, int method){
		int N = Spots.length;
		double[] growth = new double[N];
		double[] Z = new double[N];
		double[] future = new double[N];
		double [] Ba = new double[path];
		double [] Bg = new double[path];
		double [] aPayoff =new double[path];
		double [] gPayoff =new double[path];
		
		for(int j = 0; j<path;j++){
		//each path
		for (int i =0;i<N;i++){
			// each security in the basket
			if(i==0){
				Z[i] = randomGenerator.nextGaussian();
			}else{
				double Y = randomGenerator.nextGaussian();
				Z[i] = rho*Z[i-1]+Y*Math.sqrt((1-Math.pow(rho, 2)));
			}
			
			growth[i] = Math.exp(r-0.5*Math.pow(sigma[i],2)*T+sigma[i]*Math.sqrt(T)*Z[i]);
			future[i] = Spots[i]*growth[i];
		}
		
		Ba[j] = average(future);
		Bg[j] = GeoMean(future);
		
		if (method == 2){
		      double adjK = K + Bg[j] - Ba[j];
		      	aPayoff[j] = Math.exp(-r*T)* Math.max(Ba[j] -adjK,0);
		      	gPayoff[j] = Math.exp(-r*T)* Math.max(Bg[j]-adjK,0);
		    }else{	  
		    	aPayoff[j] = Math.exp(-r*T)*Math.max(Option*Ba[j] -Option*K,0);
		    	gPayoff[j]= Math.exp(-r*T)*Math.max(Option*Bg[j] -Option*K,0);
		    }
		}
	
		if (method == 0){
			
		    double Pmean = average(aPayoff);
		    double Pstd = getStdDev(aPayoff);
		    double Uconf= Pmean-1.96*Pstd/Math.sqrt(path);
		    double Lconf = Pmean+1.96*Pstd/Math.sqrt(path);
		    double[] result = {Pmean, Uconf,Lconf};
		    
		    return result;
		    
		  }else if (method == 1 || method == 2){
		    
		    double covXY = getCovar(aPayoff,gPayoff);
		    double theta = covXY/getVariance(gPayoff);
		    double geo = BasketGeometric(Option, Spots, K, T, sigma,r, rho);
		    double[] P = ControlVariateList(aPayoff, theta,geo,gPayoff);
		    double Pmean = average(P);
		    double Pstd = getStdDev(P);
		    double Uconf = Pmean-1.96*Pstd/Math.sqrt(path);
		    double Lconf = Pmean+1.96*Pstd/Math.sqrt(path);
		    
		    double[] result = {Pmean, Uconf,Lconf};
		    
		    return result;
		  }
		
		return null;
		
	}
	/** Supplementary methods**/
	
	//generating the controlVariate Matrix
	public static double[] ControlVariateList(double[] aPayoff, double theta, double geo, double[] geoPayoff){
		double[] Zi = new double[aPayoff.length];
		
			for(int i = 0; i<aPayoff.length ; i++){
				Zi[i] = aPayoff[i]+theta*(geo - geoPayoff[i]);
			}
		return Zi;
	}
	//cumulative normal distribution function
	public static double CNDF(double x){
	    int neg = (x < 0d) ? 1 : 0;
	    if ( neg == 1) 
	        x *= -1d;

	    double k = (1d / ( 1d + 0.2316419 * x));
	    double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
	                   k - 0.356563782) * k + 0.319381530) * k;
	    y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

	    return (1d - neg) * y + neg * (1d - y);
	}
	//sum of a list
	public static double Summation(double[] data){
		double temp = 0.0;
		for (double i :data){
			temp += i;
		}
		return temp;
	}
	//take log of a list
	public static double[] ListLog(double[] data){
		
		for (int i = 0; i< data.length; i++ ){
			data[i] += Math.log(data[i]);
		}
		return data;
	}
	//take mean of a list
	public static double average(double[] data){  
	    double sum = 0;

	    for(int i=0; i < data.length; i++){
	    	sum = sum + data[i]; 
	    }
	    double average = sum / data.length;
	    return average;

	}
	// take geometric Mean of a list
	public static double GeoMean(double[] data){  
	    double product = 1;

	    for(int i=0; i < data.length; i++){
	    	product = product*data[i]; 
	    }
	    double geoMean = Math.pow(product, 1.0/data.length); 
	    return geoMean;

	}


	//get variance of a list
	public static double getVariance(double[] data)
    {
        double mean = average(data);
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
            return temp/data.length;
    }
	//get covariance of 2 list
	public static double getCovar(double[] v1, double[] v2) {
	    double m1 = average(v1);
	    double m2 = average(v2);
	    double sumsq = 0.0;
	    for (int i = 0; i < v1.length; i++)
	      sumsq += (m1 - v1[i]) * (m2 - v2[i]);
	    return sumsq / (v1.length);
	  }
	//get SD of a list
	public static double getStdDev(double[] data)
    {
        return Math.sqrt(getVariance(data));
    }
	
	//Debug output in android
	
	public final static class Debug{
	    private Debug (){}

	    public static void out (Object msg){
	        Log.i ("info", msg.toString ());
	    }
	}
}
