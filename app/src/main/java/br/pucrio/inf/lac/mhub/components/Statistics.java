package br.pucrio.inf.lac.mhub.components;

import java.util.Arrays;

/**
 * Created by luizg on 03/10/2017.
 */

public class Statistics {

    private double[] data;

    public Statistics(double[] data)
    {
        this.data = data;
    }

    public double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/data.length;
    }

    private double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);

        if((data.length-1) == 0)
            return 0.0;
        else
            return temp/(data.length-1);
    }

    public double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double median()
    {
        Arrays.sort(data);

        if (data.length % 2 == 0)
        {
            return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
        }
        return data[data.length / 2];
    }
}
