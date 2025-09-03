package com.mjolkster.artifice.util.tools;

import com.mjolkster.artifice.util.wrappers.Gaussian;

import java.util.ArrayList;

public class CalculateGaussians {

    /**
     * Calculates the number of enemies for the round number based on a single gaussian curve
     * @param g The gaussian values for the curve
     * @param r The round you want to sample
     * @return Number of enemies
     */

    public static int calculate(Gaussian g, int r) {

        if (g.c <= 0) throw new IllegalArgumentException("c must be positive");

        double numerator = Math.pow((r - g.b), 2);
        double denominator = 2 * Math.pow(g.c, 2);
        double exponent = Math.exp(-(numerator/denominator));

        return Math.toIntExact(Math.round(g.a * exponent));
    }

    /**
     * Calculates the number of enemies for the round number based on several gaussian curves
     * @param gaussians a list of the gaussian values for the curves
     * @param r The round you want to sample
     * @return Number of enemies
     */

    public static int calculateMultiGaussian(ArrayList<Gaussian> gaussians, int r) {

        int total = 0;

        for (Gaussian g : gaussians) {
            if (g.c <= 0) throw new IllegalArgumentException("c must be positive");

            double numerator = Math.pow((r - g.b), 2);
            double denominator = 2 * Math.pow(g.c, 2);
            double exponent = Math.exp(-(numerator/denominator));

            total += Math.toIntExact(Math.round(g.a * exponent));
        }

        return total;
    }
}
