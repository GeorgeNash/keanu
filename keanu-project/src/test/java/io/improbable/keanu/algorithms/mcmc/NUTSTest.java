package io.improbable.keanu.algorithms.mcmc;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.testcategory.Slow;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.HalfGaussianVertex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class NUTSTest {

    private KeanuRandom random;

    @Before
    public void setup() {
        random = new KeanuRandom(1);
    }

    @Category(Slow.class)
    @Test
    public void canRecordStatisticsFromSamples() {
        double mu = 0.0;
        double sigma = 1.0;
        double initStepSize = 1;
        int maxTreeHeight = 4;
        BayesianNetwork simpleGaussian = MCMCTestDistributions.createSimpleGaussian(mu, sigma, 3, random);

        NUTS nuts = NUTS.builder()
            .adaptEnabled(false)
            .initialStepSize(initStepSize)
            .random(random)
            .maxTreeHeight(maxTreeHeight)
            .saveStatistics(true)
            .build();

        nuts.getPosteriorSamples(
            simpleGaussian,
            simpleGaussian.getLatentVertices(),
            2
        );

        Statistics statistics = nuts.getStatistics();

        List<Double> stepSize = statistics.get("stepSize");
        List<Double> logProb = statistics.get("logProb");
        List<Double> meanTreeAccept = statistics.get("meanTreeAccept");
        List<Double> treeSize = statistics.get("treeSize");

        Assert.assertTrue(stepSize.get(0) == initStepSize);
        Assert.assertTrue(stepSize.get(1) == initStepSize);
        Assert.assertTrue(logProb.get(0) < 0);
        Assert.assertTrue(logProb.get(1) < 0);
        Assert.assertTrue(meanTreeAccept.get(0) >= 0 && meanTreeAccept.get(0) <= 1);
        Assert.assertTrue(meanTreeAccept.get(1) >= 0 && meanTreeAccept.get(1) <= 1);
        Assert.assertTrue(treeSize.get(0) < Math.pow(2, maxTreeHeight));
        Assert.assertTrue(treeSize.get(1) < Math.pow(2, maxTreeHeight));
    }

    @Category(Slow.class)
    @Test
    public void samplesGaussian() {
        double mu = 0.0;
        double sigma = 1.0;
        BayesianNetwork simpleGaussian = MCMCTestDistributions.createSimpleGaussian(mu, sigma, 3, random);

        NUTS nuts = NUTS.builder()
            .adaptCount(2000)
            .random(random)
            .targetAcceptanceProb(0.65)
            .build();

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            simpleGaussian,
            simpleGaussian.getLatentVertices(),
            2000
        );

        Vertex<DoubleTensor> vertex = simpleGaussian.getContinuousLatentVertices().get(0);

        MCMCTestDistributions.samplesMatchSimpleGaussian(mu, sigma, posteriorSamples.get(vertex).asList(), 0.1);
    }

    @Category(Slow.class)
    @Test
    public void samplesHalfGaussian() {
        double sigma = 1.0;
        HalfGaussianVertex A = new HalfGaussianVertex(new long[]{1, 1}, sigma);
        A.setAndCascade(0.5);
        BayesianNetwork b = new BayesianNetwork(A.getConnectedGraph());

        NUTS nuts = NUTS.builder()
            .adaptCount(500)
            .random(random)
            .targetAcceptanceProb(0.65)
            .build();

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            b,
            b.getLatentVertices(),
            500
        );

        List<DoubleTensor> samples = posteriorSamples.get(A).asList();

    }

    @Test
    public void samplesContinuousPrior() {

        BayesianNetwork bayesNet = MCMCTestDistributions.createSumOfGaussianDistribution(20.0, 1.0, 46., 15.0);

        int sampleCount = 5000;
        NUTS nuts = NUTS.builder()
            .adaptCount(sampleCount)
            .maxTreeHeight(10)
            .targetAcceptanceProb(0.6)
            .random(random)
            .build();

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            bayesNet,
            bayesNet.getLatentVertices(),
            sampleCount
        ).drop(sampleCount / 4);

        Vertex<DoubleTensor> A = bayesNet.getContinuousLatentVertices().get(0);
        Vertex<DoubleTensor> B = bayesNet.getContinuousLatentVertices().get(1);

        MCMCTestDistributions.samplesMatchesSumOfGaussians(44.0, posteriorSamples.get(A).asList(), posteriorSamples.get(B).asList());
    }

    @Category(Slow.class)
    @Test
    public void samplesFromDonut() {
        BayesianNetwork donutBayesNet = MCMCTestDistributions.create2DDonutDistribution();

        NUTS nuts = NUTS.builder()
            .adaptCount(1000)
            .targetAcceptanceProb(0.5)
            .random(random)
            .build();

        NetworkSamples samples = nuts.getPosteriorSamples(
            donutBayesNet,
            donutBayesNet.getLatentVertices(),
            1000
        );

        Vertex<DoubleTensor> A = donutBayesNet.getContinuousLatentVertices().get(0);
        Vertex<DoubleTensor> B = donutBayesNet.getContinuousLatentVertices().get(1);

        MCMCTestDistributions.samplesMatch2DDonut(samples.get(A).asList(), samples.get(B).asList());
    }

    @Test
    public void canDefaultToSettingsInBuilderAndIsConfigurableAfterBuilding() {

        GaussianVertex A = new GaussianVertex(0.0, 1.0);
        BayesianNetwork net = new BayesianNetwork(A.getConnectedGraph());
        net.probeForNonZeroProbability(100, random);

        NUTS nuts = NUTS.builder()
            .build();

        assertTrue(nuts.getAdaptCount() > 0);
        assertTrue(nuts.getTargetAcceptanceProb() > 0);
        assertNotNull(nuts.getRandom());

        NetworkSamples posteriorSamples = nuts.getPosteriorSamples(
            net,
            net.getLatentVertices(),
            2
        );

        nuts.setRandom(null);
        assertNull(nuts.getRandom());

        assertFalse(posteriorSamples.get(A).asList().isEmpty());
    }
}
