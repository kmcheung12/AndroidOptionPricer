package com.comp7405.optionpricer;

/**
 * Created by alancheung on 28/4/14.
 */
public class StatisticHelper {
	/** Supplementary methods**/
    public static double[][] transpose(double[][] a) {
        int r = a.length;
        int c = a[0].length;
        double[][] l = new double[c][r];
        for (int i=0; i<r; i++) {
            for (int j=0; j<r; j++) {
               l[j][i] = a[i][j];
            }
        }
        return l;
    }

    public static double[][] chol(double[][] a){
        int m = a.length;
        double[][] l = new double[m][m]; //automatically initialzed to 0's
        for(int i = 0; i< m;i++){
            for(int k = 0; k < (i+1); k++){
                double sum = 0;
                for(int j = 0; j < k; j++){
                    sum += l[i][j] * l[k][j];
                }
                l[i][k] = (i == k) ? Math.sqrt(a[i][i] - sum) :
                        (1.0 / l[k][k] * (a[i][k] - sum));
            }
        }
        return l;
    }

    public static double[][] matrixMul(double[][] m1, double[][] m2) throws IllegalArgumentException{
//        int m1col = m1.length;
//        int m1row = m1[0].length;
//
//        int m2col = m2.length;
//        int m2row = m2[0].length;
//
//        if (m1col != m2row) {
//            String msg = String.format("Cannot perform matrix multiplication with dimensions (%d, %d) and (%d, %d)", m1row,m1col,m2row,m2col);
//            throw new IllegalArgumentException(msg);
//        }
//        double[][] l = new double[m2col][m1row];
//        for (int i =0; i< m1row; i++) {
//            for (int j=0; j< m2col; j++) {
//                for (int k =0; k < m1col; k ++) {
//                    l[j][i] += m1[k][j] * m2[i][k];
//                }
//            }
//        }
        return multiplicar(m1,m2);
    }

    public static double[][] multiplicar(double[][] a, double[][] b) {

        int aRows = a.length,
                aColumns = a[0].length,
                bRows = b.length,
                bColumns = b[0].length;

        if ( aColumns != bRows ) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        double[][] resultant = new double[aRows][bColumns];

        for(int i = 0; i < aRows; i++) { // aRow
            for(int j = 0; j < bColumns; j++) { // bColumn
                for(int k = 0; k < aColumns; k++) { // aColumn
                    resultant[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return resultant;
    }
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
           sum += Math.log(data[i]);
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
