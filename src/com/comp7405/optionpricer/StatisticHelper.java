package com.comp7405.optionpricer;

/**
 * Created by alancheung on 28/4/14.
 */
public class StatisticHelper {
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
    //http://stackoverflow.com/questions/442758/which-java-library-computes-the-cumulative-standard-normal-distribution-function
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
	public static double summation(double[] data){
		double sum = 0.0;
        int length = data.length;
		for (int i=0; i< length; i++){
			sum += data[i];
		}
		return sum;
	}

	//take mean of a list
	public static double arithmeticMean(double[] data){
	    double sum = 0;
        int length = data.length;
	    for(int i=0; i < length; i++){
	    	sum += data[i];
	    }
	    return sum/ data.length;
	}
	// take geometric Mean of a list
	public static double geometricMean(double[] data){
        // geoMean = exp(1/N * sum log(data[i]))
        double sum = 0;
        int length = data.length;
	    for(int i=0; i < length; i++){
           sum += Math.exp(data[i]);
	    }
        sum /= length;
        double geoMean = Math.exp(sum);
	    return geoMean;

	}


	//get variance of a list
	public static double getVariance(double[] data)
    {
        double mean = arithmeticMean(data);
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
            return temp/data.length;
    }
	//get covariance of 2 list
	public static double getCovar(double[] v1, double[] v2) {
	    double m1 = arithmeticMean(v1);
	    double m2 = arithmeticMean(v2);
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

    public static double[] confidenceInterval(double[] sample) {
        double mean = arithmeticMean(sample);
        double std = getStdDev(sample);
        int length = sample.length;
        double confUpper= mean-1.96*std/Math.sqrt(length);
        double confLower = mean+1.96*std/Math.sqrt(length);
        double[] result = {mean, confUpper,confLower};
		return result;
    }
}
