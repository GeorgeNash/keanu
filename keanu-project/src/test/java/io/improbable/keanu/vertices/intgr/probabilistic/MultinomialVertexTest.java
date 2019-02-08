package io.improbable.keanu.vertices.intgr.probabilistic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.improbable.keanu.DeterministicRule;
import io.improbable.keanu.KeanuRandom;
import io.improbable.keanu.distributions.DiscreteDistribution;
import io.improbable.keanu.distributions.discrete.Binomial;
import io.improbable.keanu.distributions.discrete.Multinomial;
import io.improbable.keanu.tensor.TensorValueException;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.generic.GenericTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.testcategory.Slow;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.LogProbGraph;
import io.improbable.keanu.vertices.LogProbGraphValueFeeder;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.multiple.ConcatenationVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary.ReshapeVertex;
import io.improbable.keanu.vertices.generic.probabilistic.discrete.CategoricalVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.utility.GraphAssertionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static io.improbable.keanu.tensor.TensorMatchers.allCloseTo;
import static io.improbable.keanu.tensor.TensorMatchers.allValues;
import static io.improbable.keanu.tensor.TensorMatchers.hasShape;
import static io.improbable.keanu.tensor.TensorMatchers.hasValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultinomialVertexTest {

    @Rule
    public DeterministicRule rule = new DeterministicRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void itThrowsIfTheProbabilitiesDontSumToOne() {
        IntegerVertex n = ConstantVertex.of(100);
        DoubleVertex p = ConstantVertex.of(0.1, 0.1, 0.1, 0.1);
        MultinomialVertex multinomialVertex = new MultinomialVertex(new long[] {4}, n, p);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Probabilities must sum to one");

        multinomialVertex.logProb(IntegerTensor.ONE_SCALAR);
    }

    @Test
    public void logProbGraphThrowsIfTheProbabilitiesDontSumToOne() {
        IntegerVertex n = ConstantVertex.of(100);
        DoubleVertex p = ConstantVertex.of(0.1, 0.1, 0.1, 0.1);
        MultinomialVertex myVertex = new MultinomialVertex(new long[] {4}, n, p);
        LogProbGraph logProbGraph = myVertex.logProbGraph();
        LogProbGraphValueFeeder.feedValue(logProbGraph, n, n.getValue());
        LogProbGraphValueFeeder.feedValue(logProbGraph, myVertex, IntegerTensor.ONE_SCALAR);

        thrown.expect(GraphAssertionException.class);
        thrown.expectMessage("Probabilities must sum to one.");

        LogProbGraphValueFeeder.feedValueAndCascade(logProbGraph, p, p.getValue());
    }

    @Test(expected = TensorValueException.class)
    public void inDebugModeItThrowsIfAnyOfTheProbabilitiesIsZero() {
        try {
            Multinomial.CATEGORY_PROBABILITIES_CANNOT_BE_ZERO.enable();
            IntegerTensor n = IntegerTensor.scalar(100).reshape(1, 1);
            DoubleTensor p = DoubleTensor.create(0., 0., 1., 0.).reshape(4, 1);
            Multinomial.withParameters(n, p);
        } finally {
            Multinomial.CATEGORY_PROBABILITIES_CANNOT_BE_ZERO.disable();
        }
    }

    @Test
    public void itThrowsIfTheParametersAreDifferentHighRankShapes() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(1, 2, 3, 4, 5, 6, 7, 8).reshape(2, 4));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.linspace(0, 1, 18).reshape(3, 2, 3));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Proposed shape [2, 4] does not match other non length one shapes [2, 3]");

        new MultinomialVertex(n, p);
    }

    @Test
    public void itThrowsIfTheParametersAreDifferentShapes() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(1, 2).reshape(1, 2));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.linspace(0, 1, 9).reshape(3, 3));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Proposed shape [1, 2] does not match other non length one shapes [3]");

        new MultinomialVertex(n, p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void itThrowsIfTheSampleShapeDoesntMatchTheShapeOfN() {
        IntegerTensor n = IntegerTensor.create(100, 200).reshape(1, 2);
        DoubleTensor p = DoubleTensor.create(new double[]{
                0.1, 0.25,
                0.2, 0.25,
                0.3, 0.25,
                0.4, 0.25
            },
            4, 2);
        Multinomial multinomial = Multinomial.withParameters(n, p);
        multinomial.sample(new long[]{2, 2}, KeanuRandom.getDefaultRandom());
    }

    @Test
    public void itThrowsIfTheLogProbShapeDoesntMatchTheNumberOfCategories() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(100).reshape(1, 1));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(0.1, 0.2, .3, 0.4).reshape(4, 1));
        MultinomialVertex multinomialVertex = new MultinomialVertex(n, p);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Shape mismatch. k: [], p: [4, 1]");

        multinomialVertex.logProb(IntegerTensor.scalar(1));
    }

    @Test
    public void itThrowsIfTheLogProbGraphShapeDoesntMatchTheNumberOfCategories() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(100).reshape(1, 1));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(0.1, 0.2, .3, 0.4).reshape(4, 1));
        MultinomialVertex multinomialVertex = new MultinomialVertex(n, p);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Shape mismatch. k: [1, 1], p: [4, 1]");

        multinomialVertex.logProbGraph();
    }

    @Test
    public void itThrowsIfTheLogProbStateDoesntSumToN() {
        IntegerTensor n = IntegerTensor.create(10, 10);
        DoubleTensor p = DoubleTensor.create(0.2, 0.8);
        IntegerTensor k = IntegerTensor.create(5, 6);
        MultinomialVertex multinomialVertex = new MultinomialVertex(ConstantVertex.of(n), ConstantVertex.of(p));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format("Inputs %s must sum to n = %s", k, n));

        multinomialVertex.logProb(k);
    }

    @Test
    public void itThrowsIfTheLogProbGraphStateDoesntSumToN() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(10, 10));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(0.2, 0.8));
        MultinomialVertex multinomialVertex = new MultinomialVertex(n, p);
        LogProbGraph logProbGraph = multinomialVertex.logProbGraph();

        LogProbGraphValueFeeder.feedValue(logProbGraph, n, n.getValue());
        LogProbGraphValueFeeder.feedValue(logProbGraph, p, p.getValue());

        thrown.expect(GraphAssertionException.class);
        thrown.expectMessage("Inputs must sum to n.");

        LogProbGraphValueFeeder.feedValueAndCascade(logProbGraph, multinomialVertex, IntegerTensor.create(5, 6));
    }

    @Test
    public void itThrowsIfTheLogProbStateContainsNegativeNumbers() {
        IntegerTensor n = IntegerTensor.create(10, 10);
        DoubleTensor p = DoubleTensor.create(0.2, 0.8);
        IntegerTensor k = IntegerTensor.create(-1, 11);
        MultinomialVertex multinomialVertex = new MultinomialVertex(ConstantVertex.of(n), ConstantVertex.of(p));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format("Inputs %s cannot be negative", k));

        multinomialVertex.logProb(k);
    }

    @Test
    public void itThrowsIfTheLogProbGraphStateContainsNegativeNumbers() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(10, 10));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(0.2, 0.8));
        MultinomialVertex multinomialVertex = new MultinomialVertex(n, p);
        LogProbGraph logProbGraph = multinomialVertex.logProbGraph();

        LogProbGraphValueFeeder.feedValue(logProbGraph, n, n.getValue());
        LogProbGraphValueFeeder.feedValue(logProbGraph, p, p.getValue());

        thrown.expect(GraphAssertionException.class);
        thrown.expectMessage("Inputs cannot be negative.");

        LogProbGraphValueFeeder.feedValueAndCascade(logProbGraph, multinomialVertex, IntegerTensor.create(-1, 11));
    }

    @Test
    public void itThrowsIfTheLogProbStateContainsNumbersGreaterThanN() {
        int[] state = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 12};
        assertThat(state[0] + state[1] + state[2], equalTo(10));
        IntegerTensor k = IntegerTensor.create(state);

        IntegerTensor n = IntegerTensor.create(10, 10, 10);
        DoubleTensor p = DoubleTensor.create(0.2, 0.3, 0.5);
        MultinomialVertex multinomialVertex = new MultinomialVertex(ConstantVertex.of(n), ConstantVertex.of(p));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format("Inputs %s must sum to n = %s", k, n));

        multinomialVertex.logProb(k);
    }

    @Test
    public void itThrowsIfTheLogProbGraphStateContainsNumbersGreaterThanN() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(10, 10, 10));
        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(0.2, 0.3, 0.5));
        MultinomialVertex multinomialVertex = new MultinomialVertex(n, p);
        LogProbGraph logProbGraph = multinomialVertex.logProbGraph();

        LogProbGraphValueFeeder.feedValue(logProbGraph, n, n.getValue());
        LogProbGraphValueFeeder.feedValue(logProbGraph, p, p.getValue());

        int[] state = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 12};
        assertThat(state[0] + state[1] + state[2], equalTo(10));
        IntegerTensor k = IntegerTensor.create(state);

        thrown.expect(GraphAssertionException.class);
        thrown.expectMessage("Inputs must sum to n.");

        LogProbGraphValueFeeder.feedValueAndCascade(logProbGraph, multinomialVertex, IntegerTensor.create(state));
    }

    @Test
    public void itWorksWithScalars() {
        int n = 100;
        DoubleTensor p = DoubleTensor.create(new double[]{0.01, 0.09, 0.9}, 3, 1);
        MultinomialVertex multinomial = new MultinomialVertex(n, ConstantVertex.of(p));
        IntegerTensor samples = multinomial.sample(KeanuRandom.getDefaultRandom());
        assertThat(samples, hasShape(3, 1));
        assertThat(samples, allValues(both(greaterThan(-1)).and(lessThan(n))));
    }

    @Test
    public void itWorksWithTensors() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(new int[]{
                1, 5, 8, 10,
                100, 200, 500, 1000},
            2, 4));

        DoubleVertex p = ConstantVertex.of(DoubleTensor.create(new double[]{
                .1, .2, .3, .8,
                .25, .25, .4, .45,

                .1, .2, .3, .1,
                .50, .25, .4, .45,

                .8, .6, .4, .1,
                .25, .5, .2, .1
            },
            3, 2, 4));
        //
        MultinomialVertex multinomial = new MultinomialVertex(n, p);
        IntegerTensor sample = multinomial.sample(KeanuRandom.getDefaultRandom());
        assertThat(sample, hasShape(3, 2, 4));
        double logProb = multinomial.logProb(IntegerTensor.create(new int[]{
                0, 1, 2, 10,
                25, 50, 200, 450,

                0, 0, 2, 0,
                50, 50, 200, 450,

                1, 4, 4, 0,
                25, 100, 100, 100,
            },
            3, 2, 4));
        assertThat(logProb, closeTo(-30.193364297395277, 1e-8));
    }

    @Test
    public void youCanUseAConcatAndReshapeVertexToPipeInTheProbabilities() {
        IntegerVertex n = ConstantVertex.of(IntegerTensor.create(new int[]{
                1, 10,
                100, 1000},
            2, 2));

        DoubleVertex p1 = ConstantVertex.of(DoubleTensor.create(new double[]{
                .1, .8,
                .25, .2,
            },
            2, 2));

        DoubleVertex p2 = ConstantVertex.of(DoubleTensor.create(new double[]{
                .1, .1,
                .50, .3,
            },
            2, 2));

        DoubleVertex p3 = ConstantVertex.of(DoubleTensor.create(new double[]{

                .8, .1,
                .25, .5
            },
            2, 2));

        ConcatenationVertex pConcatenated = new ConcatenationVertex(0, p1, p2, p3);
        ReshapeVertex pReshaped = new ReshapeVertex(pConcatenated, 3, 2, 2);
        MultinomialVertex multinomial = new MultinomialVertex(n, pReshaped);
        IntegerTensor sample = multinomial.sample(KeanuRandom.getDefaultRandom());
        assertThat(sample, hasShape(3, 2, 2));
        double logProb = multinomial.logProb(IntegerTensor.create(new int[]{
                0, 10,
                25, 200,

                0, 0,
                50, 300,

                1, 0,
                25, 500,
            },
            3, 2, 2));

        assertThat(logProb, equalTo(-14.165389164658901));
    }


    @Test
    public void youCanSampleWithATensorIfNIsScalarAndPIsAColumnVector() {
        int n = 100;
        DoubleTensor p = DoubleTensor.create(0.1, 0.2, .3, 0.4).reshape(4, 1);
        Multinomial multinomial = Multinomial.withParameters(IntegerTensor.scalar(n).reshape(1, 1), p);
        IntegerTensor samples = multinomial.sample(new long[]{2, 2}, KeanuRandom.getDefaultRandom());
        assertThat(samples, hasShape(4, 2, 2));
        assertThat(samples, allValues(both(greaterThan(-1)).and(lessThan(n))));
    }

    @Test
    public void ifYourRandomReturnsZeroItSamplesFromTheFirstCategory() {
        KeanuRandom mockRandomAlwaysZero = mock(KeanuRandom.class);
        when(mockRandomAlwaysZero.nextDouble()).thenReturn(0.);
        IntegerTensor n = IntegerTensor.scalar(100).reshape(1, 1);
        DoubleTensor p = DoubleTensor.create(0.1, 0.2, .3, 0.4).reshape(4, 1);
        Multinomial multinomial = Multinomial.withParameters(n, p);
        IntegerTensor samples = multinomial.sample(new long[]{1, 1}, mockRandomAlwaysZero);
        assertThat(samples, hasValue(100, 0, 0, 0));
    }

    @Test
    public void ifYourRandomReturnsOneItSamplesFromTheLastCategory() {
        KeanuRandom mockRandomAlwaysZero = mock(KeanuRandom.class);
        when(mockRandomAlwaysZero.nextDouble()).thenReturn(1.);
        IntegerTensor n = IntegerTensor.scalar(100).reshape(1, 1);
        DoubleTensor p = DoubleTensor.create(0.1, 0.2, .3, 0.4).reshape(4, 1);
        Multinomial multinomial = Multinomial.withParameters(n, p);
        IntegerTensor samples = multinomial.sample(new long[]{1, 1}, mockRandomAlwaysZero);
        assertThat(samples, hasValue(0, 0, 0, 100));
    }

    @Test
    public void whenKEqualsTwoItsBinomial() {
        IntegerTensor n = IntegerTensor.scalar(10).reshape(1, 1);
        DoubleTensor p = DoubleTensor.create(0.2, 0.8).reshape(2, 1);
        DiscreteDistribution multinomial = Multinomial.withParameters(n, p);
        DiscreteDistribution binomial = Binomial.withParameters(DoubleTensor.scalar(0.2), n);
        for (int value : ImmutableList.of(1, 2, 9, 10)) {
            DoubleTensor binomialLogProbs = binomial.logProb(IntegerTensor.scalar(value));
            DoubleTensor multinomialLogProbs = multinomial.logProb(IntegerTensor.create(value, 10 - value).reshape(2, 1)).transpose();
            assertThat(multinomialLogProbs, allCloseTo(1e-6, binomialLogProbs));
        }
    }

    enum Color {
        RED, GREEN, BLUE
    }

    @Test
    public void whenKNEqualsOneItsCategorical() {
        IntegerTensor n = IntegerTensor.scalar(1).reshape(1, 1);
        DoubleTensor p = DoubleTensor.create(0.2, .3, 0.5).reshape(3, 1);
        DiscreteDistribution multinomial = Multinomial.withParameters(n, p);

        Map<Color, DoubleVertex> selectableValues = ImmutableMap.of(
            Color.RED, ConstantVertex.of(p.getValue(0)),
            Color.GREEN, ConstantVertex.of(p.getValue(1)),
            Color.BLUE, ConstantVertex.of(p.getValue(2)));
        CategoricalVertex<Color, GenericTensor<Color>> categoricalVertex = new CategoricalVertex<>(selectableValues);

        double pRed = categoricalVertex.logProb(GenericTensor.scalar(Color.RED));
        assertThat(multinomial.logProb(IntegerTensor.create(1, 0, 0).reshape(3, 1)).scalar(), closeTo(pRed, 1e-7));
        double pGreen = categoricalVertex.logProb(GenericTensor.scalar(Color.GREEN));
        assertThat(multinomial.logProb(IntegerTensor.create(0, 1, 0).reshape(3, 1)).scalar(), closeTo(pGreen, 1e-7));
        double pBlue = categoricalVertex.logProb(GenericTensor.scalar(Color.BLUE));
        assertThat(multinomial.logProb(IntegerTensor.create(0, 0, 1).reshape(3, 1)).scalar(), closeTo(pBlue, 1e-7));
    }

    @Category(Slow.class)
    @Test
    public void samplingProducesRealisticMeanAndStandardDeviation() {
        int N = 10000;
        DoubleTensor p = DoubleTensor.create(0.1, 0.2, 0.3, 0.4).reshape(4, 1);
        IntegerTensor n = IntegerTensor.scalar(500).reshape(1, 1);

        MultinomialVertex vertex = new MultinomialVertex(
            new long[]{1, N},
            ConstantVertex.of(n),
            ConstantVertex.of(p)
        );

        IntegerTensor samples = vertex.sample();
        assertThat(samples, hasShape(4, N));

        for (int i = 0; i < samples.getShape()[0]; i++) {
            IntegerTensor sample = samples.slice(0, i);
            Double probability = p.slice(0, i).scalar();
            double mean = sample.toDouble().average();
            double std = sample.toDouble().standardDeviation();

            double epsilonForMean = 0.5;
            double epsilonForVariance = 5.;
            assertEquals(n.scalar() * probability, mean, epsilonForMean);
            assertEquals(n.scalar() * probability * (1 - probability), std * std, epsilonForVariance);
        }
    }
}
