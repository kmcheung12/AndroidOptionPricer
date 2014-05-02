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

	public double europeanOptions(OptionType optionType, double spot, double strike, double timeToMature, double t, double sigma, double interestRate){
        if (optionType == null || spot < 0 || strike < 0 || timeToMature < 0 || t < 0 || sigma >1 || interestRate >1) {
            throw new IllegalArgumentException("Invalid input");
        }
        double option   = optionType == OptionType.CALL ? 1.0 : -1.0;
		double dt       = timeToMature -t;
		double d1       = (Math.log(spot/strike)+(interestRate+Math.pow(sigma, 2)/2)*dt)/(sigma*Math.sqrt(dt));
		double d2       = d1 - sigma*Math.sqrt(dt);
		double Price    = -1*option*strike*Math.exp(-interestRate*dt)*CNDF(d2*option) + option* spot*CNDF(d1 *option);
	
		return Price;
		
	}
	
	public  double asianGeometric(OptionType optionType, double spot, double strike, double timeToMature, double sigma, double interestRate, double observation){
        if (optionType == null || spot < 0|| strike < 0 || timeToMature <0|| sigma >1 || interestRate > 1 || observation < 0) {
            throw new IllegalArgumentException("Invalid input");
        }
        double option   = optionType == OptionType.CALL ? 1.0 : -1.0;
        double sigmaHat = sigma * (Math.sqrt((observation+1)*(2*observation+1)/(6*observation*observation)));
        double muHat    = (interestRate - 0.5 * Math.pow(sigma,2)) * (observation+1)/(2*observation) + 0.5* Math.pow(sigmaHat,2);
        double d1Hat    = (Math.log(spot/strike) + (muHat + 0.5* Math.pow(sigmaHat, 2))*timeToMature)/ (sigmaHat*Math.sqrt(timeToMature));
        double d2Hat    = d1Hat - sigmaHat*Math.sqrt(timeToMature);
        double N1       = CNDF(option*d1Hat);
        double N2       = CNDF(option*d2Hat);
        double price    = Math.exp(-interestRate*timeToMature) * (option * spot * Math.exp(muHat*timeToMature) * N1 - option*strike* N2);
        return price;
	}

/*
Parameters:
OptionType optionType   - enum defines pricing of PUT or CALL option
double spot             - spot price of asset
double strike           - strike price of option
double timeToMature     - time to maturity of option
double sigma            - volatility of asset
double interestRate     - risk free interest rate
int observation         - number of time for observation till time to mature
int path                - number of simulations
PricerMethod method     - enum defining simulation method. One of the follow value:STANDARD, CONTROL_VARIATE or ADJUSTED_STRIKE
*/

	public double[] asianArithmetic(OptionType optionType , double spot, double strike, double timeToMature, double sigma, double interestRate, int observation, int path, PricerMethod method){
        if (optionType ==null|| spot < 0 || strike < 0 || timeToMature <0 || sigma >1 ||interestRate >1 || observation <0 ||path <0||method == null) {
            throw new IllegalArgumentException("Invalid input");
        }

        double option       = optionType == OptionType.CALL ? 1.0 : -1.0;
        double[] stockPath  = new double[observation];
		double[] aPayoff    = new double[path];
		double[] gPayoff    = new double[path];
	
		double dt           = timeToMature/observation;
		double drift        = Math.exp((interestRate-0.5*Math.pow(sigma,2))*dt);
        double growth       = 0;

        double sigmaHat     = sigma * (Math.sqrt((observation + 1) * (2 * observation + 1) / ((float)6 * observation * observation)));
        double muHat        = (interestRate - 0.5 * sigma * sigma) * (observation + 1) / (2 * observation) + 0.5 * sigmaHat * sigmaHat;

        double eAsianA      = 0;
        //E_AsianA = S* sum(exp([1:n]*dt*r))/n;
        for (int i =1; i<= observation; i++) {
            eAsianA += Math.exp(i*dt*interestRate);
        }
        eAsianA             = eAsianA*spot/observation;
        double eAsianG      = spot * Math.exp(muHat * timeToMature);
        double adjK         = strike;
        if (method == PricerMethod.ADJUSTED_STRIKE){
            adjK = strike + eAsianG - eAsianA;
        }
        for ( int i = 0; i<path; i++){
            if (path > 20 && i%(path/20) == 0) {
                if (listener != null) {
                    listener.onProgessChange((float)i/path);
                }
            }
            for (int j = 0; j<observation; j++){
                growth = drift * Math.exp(sigma*Math.sqrt(dt)*randomGenerator.nextGaussian());

                if (j == 0){
                  stockPath [0] = spot * growth;
              }else{
                  stockPath[j] = stockPath[j-1] * growth;
              }

            }

            double aMean = arithmeticMean(stockPath);
            double gMean = geometricMean(stockPath);

            aPayoff[i] = Math.exp(-interestRate*timeToMature)* Math.max(option * (aMean - strike),0);
            gPayoff[i] = Math.exp(-interestRate*timeToMature)* Math.max(option * (gMean - adjK),0);
        }
		  
        if (method == PricerMethod.STANDARD){

		    return confidenceInterval(aPayoff);
		    
        } else if (method == PricerMethod.CONTROL_VARIATE || method == PricerMethod.ADJUSTED_STRIKE){
		    
		    double covXY = getCovar(aPayoff,gPayoff);
		    double theta = covXY/getVariance(gPayoff);
		    double geo = asianGeometric(optionType, spot, adjK, timeToMature, sigma, interestRate, observation);
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

/*
Parameters:
OptionType optionType   - enum defines pricing of PUT or CALL option
double[] spots          - array of spot prices of asset
double strike           - strike price of option
double timeToMature     - time to maturity of option
double[] sigma          - array volatilities of asset
double interestRate     - risk free interest rate
double[][] rhos         - Correlation matix for generating correlated random numbers from uncorrelation random numbers, with the help of cholsky decomposition
int path                - number of simulations
PricerMethod method     - enum defining simulation method. One of the follow value:STANDARD, CONTROL_VARIATE or ADJUSTED_STRIKE
*/
	public double basketGeometric(OptionType optionType, double[] spots, double strike, double timeToMature, double[] sigmas, double interestRate, double[][] rhos){
		if (optionType == null || spots == null || strike < 0 ||timeToMature < 0|| sigmas ==null || interestRate >1|| rhos == null) {
            throw new IllegalArgumentException("Invalid inputs");
        }

        double option   = optionType == OptionType.CALL?1.0:-1.0;

        double sigmaB   = geometricBasketVolatility(sigmas, rhos);
		double muB      = geometricBasketDrift(sigmas,sigmaB,interestRate);

		double Bg0      = geometricMean(spots);
		double d1Hat    = (Math.log(Bg0/strike) + (muB + 0.5 * Math.pow(sigmaB,2))*timeToMature)/(sigmaB*Math.sqrt(timeToMature));
		double d2Hat    = d1Hat - sigmaB*Math.sqrt(timeToMature);
		double N1       = CNDF(option*d1Hat);
		double N2       = CNDF(option*d2Hat);
		double price    = Math.exp(-interestRate*timeToMature) * (option*Bg0*Math.exp(muB*timeToMature)*N1- option*strike*N2);
		
		return price;
}
	
	public double[] basketArithmetic(OptionType optionType, double[] spots, double strike, double timeToMature, double[] sigmas, double r, double[][] rhos, int path, PricerMethod method){
        if (optionType == null || spots == null || strike < 0 || timeToMature < 0 || sigmas == null || r > 1 || rhos == null || path <0 || method == null) {
            throw new IllegalArgumentException("Invalid input");
        }

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
            drifts[i] = Math.exp((r-0.5*sigmas[i]*sigmas[i])*timeToMature);
        }

        double sigmaB   = geometricBasketVolatility(sigmas, rhos);
        double muB      = geometricBasketDrift(sigmas, sigmaB, r);

        double forwardG = Math.exp(muB * timeToMature);
        double forwardA = Math.exp(r * timeToMature);
        double discount  = Math.exp(-r*timeToMature);

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

        double adjK = strike;
        if (method == PricerMethod.ADJUSTED_STRIKE) {
                adjK = strike + eBasketG - eBasketA;
        }

        for (int i =0; i< path; i++) {

            if (path > 20 && i%(path/20) == 0) {
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
            double sqT = Math.sqrt(timeToMature);
            for (int j = 0; j<N; j++) {
                growth[j] = drifts[j] * Math.exp(sigmas[j] * sqT * corrSamples[0][j]);
                stocks[j] = spots[j] * growth[j];
            }


            basketA = arithmeticMean(stocks);
            basketG = geometricMean(stocks);



            aPayoff[i] = Math.max(option * (basketA - strike) *discount, 0) ;
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
                double geo = basketGeometric(optionType, spots, adjK, timeToMature, sigmas, r, rhos);
                double[] Z = ControlVariateList(aPayoff, theta,geo,gPayoff);
                result = confidenceInterval(Z);
            default:
                break;
        }
		return result;
		
	}
}
