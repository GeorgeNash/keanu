package io.improbable.keanu.model.regression;

import java.util.function.Function;

import io.improbable.keanu.model.ModelFitter;
import io.improbable.keanu.model.SamplingModelFitting;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;

/**
 * Builder class for doing linear regression without regularisation.
 *
 * @see RegressionModel
 */
public class RegressionModelBuilder<OUTPUT> {
    private static final double DEFAULT_MU = 0.0;
    private static final double DEFAULT_SCALE_PARAMETER = 1.0;

    private RegressionRegularization regularization = RegressionRegularization.NONE;
    private DoubleVertex priorOnWeightsScaleParameters;
    private DoubleVertex priorOnWeightsMeans;
    private DoubleVertex priorOnInterceptScaleParameter;
    private DoubleVertex priorOnInterceptMean;
    private DoubleTensor inputTrainingData;
    private OUTPUT outputTrainingData;
    private Function<DoubleVertex, LinearRegressionGraph.OutputVertices<OUTPUT>> outputTransform;
    private SamplingModelFitting samplingAlgorithm = null;

    public RegressionModelBuilder(DoubleTensor inputTrainingData, OUTPUT outputTrainingData, Function<DoubleVertex, LinearRegressionGraph.OutputVertices<OUTPUT>> outputTransform) {
        this.inputTrainingData = inputTrainingData;
        this.outputTrainingData = outputTrainingData;
        this.outputTransform = outputTransform;
    }

    public RegressionModelBuilder withRegularization(RegressionRegularization regularization) {
        this.regularization = regularization;
        return this;
    }

    /**
     * Set the input parameters to the distribution describing the prior belief about the weights of the regression model
     *
     * @param means An array of means of the distribution describing the prior belief about the regression weights
     * @param scaleParameters An array of scale parameters of the distribution describing the prior belief about the regression weights.
     *                        This will represent sigmas if no or ridge regularization is used and will represent betas if lasso regularization is used.
     * @return this
     */
    public RegressionModelBuilder withPriorOnWeights(DoubleVertex means, DoubleVertex scaleParameters) {
        RegressionWeights.checkArrayHasCorrectNumberOfFeatures(means.getShape(), getFeatureCount());
        RegressionWeights.checkArrayHasCorrectNumberOfFeatures(scaleParameters.getShape(), getFeatureCount());

        this.priorOnWeightsMeans = means;
        this.priorOnWeightsScaleParameters = scaleParameters;

        return this;
    }

    public RegressionModelBuilder withPriorOnWeights(double means, double scaleParameters) {
        return withPriorOnIntercept(ConstantVertex.of(means), ConstantVertex.of(scaleParameters));
    }

    /**
     * Set the input parameters to the distribution describing the prior belief about the intercept of the regression model
     *
     * @param mean The mean of the distribution describing the prior belief about the regression intercept
     * @param scaleParameter The scale parameter of the distribution describing the prior belief about the regression intercept.
     *                       This will represent sigmas if no or ridge regularization is used and will represent betas if lasso regularization is used.
     * @return this
     */
    public RegressionModelBuilder withPriorOnIntercept(double mean, double scaleParameter) {
        return withPriorOnIntercept(ConstantVertex.of(mean), ConstantVertex.of(scaleParameter));
    }

    public RegressionModelBuilder withPriorOnIntercept(DoubleVertex mean, DoubleVertex scaleParameter) {
        this.priorOnInterceptMean = mean;
        this.priorOnInterceptScaleParameter = scaleParameter;
        return this;
    }

    /**
     * Set the input parameters to the distribution describing the prior belief about both the intercept and weights of the regression model
     *
     * @param mean The mean of the distribution describing the prior belief about both the regression intercept and weights
     * @param scaleParameter The scale parameter of the distribution describing the prior belief about both regression intercept and weights.
     *                       This will represent sigmas if no or ridge regularization is used and will represent betas if lasso regularization is used.
     * @return this
     */
    public RegressionModelBuilder withPriorOnWeightsAndIntercept(double mean, double scaleParameter) {
        withPriorOnWeights(
            ConstantVertex.of(RegressionWeights.fillPriorOnWeights(this.inputTrainingData.getShape(), mean)),
            ConstantVertex.of(RegressionWeights.fillPriorOnWeights(this.inputTrainingData.getShape(), scaleParameter))
        );
        withPriorOnIntercept(mean, scaleParameter);
        return this;
    }

    /**
     * Optional - use a sampling algorithm to fit the model instead of the default, which is gradient optimization.
     * @param sampling Defines the number of samples to take and the algorithm to use, e.g. {@link io.improbable.keanu.algorithms.mcmc.MetropolisHastings}
     * @return this
     */
    public RegressionModelBuilder withSampling(SamplingModelFitting sampling) {
        this.samplingAlgorithm = sampling;
        return this;
    }
    /**
     * @return A linear regression model from the data passed to the builder
     */
    public RegressionModel<OUTPUT> build() {
        checkVariablesAreCorrectlyInitialised();

        LinearRegressionGraph<OUTPUT> regressionGraph = new LinearRegressionGraph<>(
                this.inputTrainingData.getShape(),
                outputTransform,
                getInterceptVertex(),
                getWeightsVertex()
        );

        ModelFitter<DoubleTensor, OUTPUT> fitter = samplingAlgorithm == null ?
            this.regularization.createFitterForGraph(regressionGraph) :
            samplingAlgorithm.createFitterForGraph(regressionGraph);

        return new RegressionModel<>(regressionGraph, inputTrainingData, outputTrainingData, fitter);
    }

    private void checkVariablesAreCorrectlyInitialised() {
        if (inputTrainingData == null) {
            throw new IllegalArgumentException("You have not provided input training data");
        }
        if (outputTrainingData == null) {
            throw new IllegalArgumentException("You have not provided output training data");
        }
        if (priorOnWeightsMeans == null || priorOnWeightsScaleParameters == null) {
            withPriorOnWeights(
                ConstantVertex.of(RegressionWeights.fillPriorOnWeights(this.inputTrainingData.getShape(), DEFAULT_MU)),
                ConstantVertex.of(RegressionWeights.fillPriorOnWeights(this.inputTrainingData.getShape(), DEFAULT_SCALE_PARAMETER))
            );
        }
        if (priorOnInterceptMean == null || priorOnInterceptScaleParameter == null) {
            withPriorOnIntercept(DEFAULT_MU, DEFAULT_SCALE_PARAMETER);
        }
    }

    private DoubleVertex getInterceptVertex() {
        return this.regularization.getInterceptVertex(priorOnInterceptMean, priorOnInterceptScaleParameter);
    }

    private DoubleVertex getWeightsVertex() {
        return this.regularization.getWeightsVertex(getFeatureCount(), priorOnWeightsMeans, priorOnWeightsScaleParameters);
    }

    private void performDataFitting(LinearRegressionGraph<OUTPUT> regressionGraph, OUTPUT outputTrainingData) {
        ModelFitter<DoubleTensor, OUTPUT> fitter = samplingAlgorithm == null ?
            this.regularization.createFitterForGraph(regressionGraph) :
            samplingAlgorithm.createFitterForGraph(regressionGraph);

        fitter.fit(inputTrainingData, outputTrainingData);
    }

    private long getFeatureCount() {
        return this.inputTrainingData.getShape()[0];
    }

}
