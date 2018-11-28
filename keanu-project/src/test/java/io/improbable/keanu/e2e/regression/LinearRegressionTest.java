package io.improbable.keanu.e2e.regression;

import io.improbable.keanu.DeterministicRule;
import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.mcmc.NUTS;
import io.improbable.keanu.algorithms.mcmc.proposal.GaussianProposalDistribution;
import io.improbable.keanu.algorithms.mcmc.proposal.MHStepVariableSelector;
import io.improbable.keanu.algorithms.mcmc.proposal.ProposalDistribution;
import io.improbable.keanu.algorithms.variational.optimizer.Optimizer;
import io.improbable.keanu.algorithms.variational.optimizer.gradient.GradientOptimizer;
import io.improbable.keanu.model.SamplingModelFitting;
import io.improbable.keanu.model.regression.RegressionModel;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.testcategory.Slow;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.improbable.keanu.e2e.regression.LinearRegressionTestUtils.assertSampledWeightsAndInterceptMatchTestData;
import static io.improbable.keanu.e2e.regression.LinearRegressionTestUtils.assertWeightsAndInterceptMatchTestData;

public class LinearRegressionTest {

    @Rule
    public DeterministicRule deterministicRule = new DeterministicRule();

    @Category(Slow.class)
    @Test
    public void manuallyBuiltGraphFindsParamsForOneWeight() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateSingleFeatureData();

        DoubleVertex weight = new GaussianVertex(0, 10.0);
        DoubleVertex intercept = new GaussianVertex(0, 10.0);
        DoubleVertex x = ConstantVertex.of(data.xTrain);
        DoubleVertex y = new GaussianVertex(x.multiply(weight).plus(intercept), 5.0);
        y.observe(data.yTrain);

        Optimizer optimizer = Optimizer.of(weight.getConnectedGraph());
        optimizer.maxLikelihood();

        assertWeightsAndInterceptMatchTestData(
            weight.getValue(),
            intercept.getValue().scalar(),
            data
        );
    }

    @Category(Slow.class)
    @Test
    public void modelFindsParamsForOneWeight() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateSingleFeatureData();

        RegressionModel linearRegressionModel = RegressionModel.withTrainingData(data.xTrain, data.yTrain)
            .build();

        linearRegressionModel.observe();
        linearRegressionModel.fit();

        assertWeightsAndInterceptMatchTestData(
            linearRegressionModel.getWeights(),
            linearRegressionModel.getIntercept(),
            data
        );
    }

    @Category(Slow.class)
    @Test
    public void manuallyBuiltGraphFindsParamsForTwoWeights() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateTwoFeatureData();

        DoubleVertex w1 = new GaussianVertex(0.0, 10.0);
        DoubleVertex w2 = new GaussianVertex(0.0, 10.0);
        DoubleVertex b = new GaussianVertex(0.0, 10.0);
        DoubleVertex x1 = ConstantVertex.of(data.xTrain.slice(0, 0));
        DoubleVertex x2 = ConstantVertex.of(data.xTrain.slice(0, 1));
        DoubleVertex y = new GaussianVertex(x1.multiply(w1).plus(x2.multiply(w2)).plus(b), 5.0);
        y.observe(data.yTrain);

        BayesianNetwork bayesNet = new BayesianNetwork(y.getConnectedGraph());
        GradientOptimizer optimizer = GradientOptimizer.of(bayesNet);

        optimizer.maxLikelihood();

        assertWeightsAndInterceptMatchTestData(
            DoubleTensor.concat(0, w1.getValue(), w2.getValue()),
            b.getValue().scalar(),
            data
        );
    }

    @Category(Slow.class)
    @Test
    public void modelFindsParamsForTwoWeights() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateTwoFeatureData();
        RegressionModel linearRegressionModel = RegressionModel.withTrainingData(data.xTrain, data.yTrain)
            .build();

        linearRegressionModel.observe();
        linearRegressionModel.fit();

        assertWeightsAndInterceptMatchTestData(
            linearRegressionModel.getWeights(),
            linearRegressionModel.getIntercept(),
            data
        );
    }

    @Category(Slow.class)
    @Test
    public void manuallyBuiltGraphFindsParamsForManyWeights() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateMultiFeatureDataUniformWeights(40);

        DoubleVertex weights = new GaussianVertex(new long[]{1, 40}, 0, 1);
        DoubleVertex intercept = new GaussianVertex(0, 1);
        DoubleVertex x = ConstantVertex.of(data.xTrain);
        DoubleVertex y = new GaussianVertex(weights.matrixMultiply(x).plus(intercept), 1);
        y.observe(data.yTrain);

        BayesianNetwork bayesNet = new BayesianNetwork(y.getConnectedGraph());
        GradientOptimizer optimizer = GradientOptimizer.of(bayesNet);
        optimizer.maxLikelihood();

        assertWeightsAndInterceptMatchTestData(
            weights.getValue(),
            intercept.getValue().scalar(),
            data
        );
    }

    @Test
    public void modelFindsParamsForManyWeights() {
        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateMultiFeatureDataUniformWeights(20);

        RegressionModel linearRegressionModel = RegressionModel.withTrainingData(data.xTrain, data.yTrain)
            .build();

        linearRegressionModel.observe();
        linearRegressionModel.fit();

        assertWeightsAndInterceptMatchTestData(
            linearRegressionModel.getWeights(),
            linearRegressionModel.getIntercept(),
            data
        );
    }

    @Category(Slow.class)
    @Test
    public void youCanChooseSamplingInsteadOfGradientOptimization() {
        final int smallRawDataSize = 20;
        final int samplingCount = 500;

        LinearRegressionTestUtils.TestData data = LinearRegressionTestUtils.generateSingleFeatureData(smallRawDataSize);

        SamplingModelFitting sampling = new SamplingModelFitting(NUTS.builder()
            .initialStepSize(0.1)
            .maxTreeHeight(10)
            .adaptCount(0)
            .adaptEnabled(false)
            .build(),
            samplingCount);

        RegressionModel linearRegressionModel = RegressionModel.withTrainingData(data.xTrain, data.yTrain)
            .withPriorOnIntercept(0, 20)
            .withPriorOnWeights(0, 20)
            .withSampling(sampling)
            .build();

        linearRegressionModel.getWeightVertex().setValue(5.);
        linearRegressionModel.getInterceptVertex().setValue(5.);

        linearRegressionModel.observe();
        linearRegressionModel.fit();

        NetworkSamples networkSamples = sampling.getNetworkSamples();

        assertSampledWeightsAndInterceptMatchTestData(
            networkSamples.getDoubleTensorSamples(linearRegressionModel.getWeightsVertexId()),
            networkSamples.getDoubleTensorSamples(linearRegressionModel.getInterceptVertexId()),
            data);
    }
}