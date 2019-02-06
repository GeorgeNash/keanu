package io.improbable.keanu.algorithms.mcmc.nuts;

import io.improbable.keanu.KeanuRandom;
import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.ProbabilisticModelWithGradient;
import io.improbable.keanu.algorithms.mcmc.testcases.MCMCTestDistributions;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.network.KeanuProbabilisticModelWithGradient;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.vis.Vizer;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NutsTestVis {

    public static void main(String[] args) {
        new NutsTestVis().showSingleGaussian();
    }


    public void showSingleGaussian() {
        KeanuRandom.setDefaultRandomSeed(0);
        double mu = 0.0;
        double sigma = 1.0;
        BayesianNetwork simpleGaussian = MCMCTestDistributions.createSimpleGaussian(mu, sigma, 0, KeanuRandom.getDefaultRandom());
        ProbabilisticModelWithGradient model = new KeanuProbabilisticModelWithGradient(simpleGaussian);

        NUTS nuts = NUTS.builder()
            .targetAcceptanceProb(0.65)
            .maxTreeHeight(15)
            .saveStatistics(true)
            .build();

        int sampleCount = 5000;
        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            model,
            model.getLatentVariables(),
            sampleCount
        );


        GaussianVertex gaussianVertex = new GaussianVertex(0, 1);
        List<Double> samplesDirect = new ArrayList<>();
        long[] shape = new long[0];
        for (int i = 0; i < sampleCount; i++) {
            samplesDirect.add(gaussianVertex.sampleWithShape(shape, KeanuRandom.getDefaultRandom()).scalar());
        }

        double averageMTA = nuts.getStatistics().get(NUTS.Metrics.MEAN_TREE_ACCEPT).stream().mapToDouble(v -> v).average().getAsDouble();
        double averageTreeSize = nuts.getStatistics().get(NUTS.Metrics.TREE_SIZE).stream().mapToDouble(v -> v).average().getAsDouble();
        double averageStepSize = nuts.getStatistics().get(NUTS.Metrics.STEPSIZE).stream().mapToDouble(v -> v).average().getAsDouble();

        Vertex<DoubleTensor> vertex = simpleGaussian.getContinuousLatentVertices().get(0);

        List<Double> samples = posteriorSamples.getDoubleTensorSamples(vertex).asList().stream().map(t -> t.scalar()).collect(Collectors.toList());

        SummaryStatistics stats = new SummaryStatistics();
        samples.forEach(stats::addValue);

        SummaryStatistics statsDirect= new SummaryStatistics();
        samplesDirect.forEach(statsDirect::addValue);

        Vizer.histogram(samples, "NUTS");
        Vizer.histogram(samplesDirect, "Direct");
    }
}