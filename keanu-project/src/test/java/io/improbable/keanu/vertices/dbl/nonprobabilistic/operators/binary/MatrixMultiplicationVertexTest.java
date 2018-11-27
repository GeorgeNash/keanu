package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import com.google.common.collect.ImmutableList;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.Differentiator;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.TensorTestOperations.finiteDifferenceMatchesForwardAndReverseModeGradient;
import static org.junit.Assert.assertEquals;

public class MatrixMultiplicationVertexTest {

    @Test
    public void canSimpleMatrixMultiply() {
        DoubleTensor matrixA = DoubleTensor.create(new double[]{1, 2, 3, 4}, 2, 2);
        DoubleTensor matrixB = DoubleTensor.create(new double[]{2, 4, 6, 8}, 2, 2);

        MatrixMultiplicationVertex mmul = new MatrixMultiplicationVertex(ConstantVertex.of(matrixA), ConstantVertex.of(matrixB));

        DoubleTensor mmulResult = mmul.lazyEval();

        DoubleTensor expected = DoubleTensor.create(new double[]{14, 20, 30, 44}, 2, 2);

        assertEquals(expected, mmulResult);
    }

    @Test
    public void canDoMatrixMultiply2x2() {
        DoubleVertex a = new UniformVertex(0, 10);
        a.setValue(DoubleTensor.create(new double[]{1, 2, 3, 4}, 2, 2));

        DoubleVertex b = new UniformVertex(0, 10);
        b.setValue(DoubleTensor.create(new double[]{5, 6, 7, 8}, 2, 2));

        MatrixMultiplicationVertex c = a.matrixMultiply(b);

        //of c wrt a,b
        DoubleTensor dCda = c.getDerivativeWrtLatents().withRespectTo(a);
        DoubleTensor dCdb = c.getDerivativeWrtLatents().withRespectTo(b);

        PartialDerivatives dCdxReverse = Differentiator.reverseModeAutoDiff(c, new HashSet<>(Arrays.asList(a, b)));
        DoubleTensor dCdaReverse = dCdxReverse.withRespectTo(a);
        DoubleTensor dCdbReverse = dCdxReverse.withRespectTo(b);

        DoubleTensor expecteddCda = DoubleTensor.create(new double[]{
            5, 7,
            0, 0,
            6, 8,
            0, 0,
            0, 0,
            5, 7,
            0, 0,
            6, 8
        }, new long[]{2, 2, 2, 2});

        DoubleTensor expecteddCdb = DoubleTensor.create(new double[]{
            1, 0,
            2, 0,
            0, 1,
            0, 2,
            3, 0,
            4, 0,
            0, 3,
            0, 4
        }, new long[]{2, 2, 2, 2});

        //of d wrt a,b
        assertEquals(expecteddCda, dCda);
        assertEquals(expecteddCdb, dCdb);
        assertEquals(expecteddCda, dCdaReverse);
        assertEquals(expecteddCdb, dCdbReverse);

        MatrixMultiplicationVertex d = b.matrixMultiply(a);

        DoubleTensor dDda = d.getDerivativeWrtLatents().withRespectTo(a);
        DoubleTensor dDdb = d.getDerivativeWrtLatents().withRespectTo(b);

        PartialDerivatives dDdxReverse = Differentiator.reverseModeAutoDiff(d, new HashSet<>(Arrays.asList(a, b)));
        DoubleTensor dDdaReverse = dDdxReverse.withRespectTo(a);
        DoubleTensor dDdbReverse = dDdxReverse.withRespectTo(b);

        DoubleTensor expecteddDda = DoubleTensor.create(new double[]{
            5, 0,
            6, 0,
            0, 5,
            0, 6,
            7, 0,
            8, 0,
            0, 7,
            0, 8
        }, new long[]{2, 2, 2, 2});

        DoubleTensor expecteddDdb = DoubleTensor.create(new double[]{
            1, 3,
            0, 0,
            2, 4,
            0, 0,
            0, 0,
            1, 3,
            0, 0,
            2, 4
        }, new long[]{2, 2, 2, 2});

        assertEquals(expecteddDda, dDda);
        assertEquals(expecteddDdb, dDdb);
        assertEquals(expecteddDda, dDdaReverse);
        assertEquals(expecteddDdb, dDdbReverse);

        AdditionVertex e = c.plus(d);

        //of e wrt a, b
        DoubleTensor dEda = e.getDerivativeWrtLatents().withRespectTo(a);
        DoubleTensor dEdb = e.getDerivativeWrtLatents().withRespectTo(b);

        PartialDerivatives dEdxReverse = Differentiator.reverseModeAutoDiff(e, new HashSet<>(Arrays.asList(a, b)));
        DoubleTensor dEdaReverse = dEdxReverse.withRespectTo(a);
        DoubleTensor dEdbReverse = dEdxReverse.withRespectTo(b);

        DoubleTensor expecteddEda = expecteddDda.plus(expecteddCda);
        DoubleTensor expecteddEdb = expecteddDdb.plus(expecteddCdb);

        assertEquals(expecteddEda, dEda);
        assertEquals(expecteddEdb, dEdb);
        assertEquals(expecteddEda, dEdaReverse);
        assertEquals(expecteddEdb, dEdbReverse);
    }

    @Test
    public void canDoMatrixMultiplyAutoDiff() {

        DoubleVertex m = new UniformVertex(0, 10);
        m.setValue(DoubleTensor.create(new double[]{1, 2}, 1, 2));

        DoubleVertex alpha = new UniformVertex(0, 10);
        alpha.setValue(DoubleTensor.create(new double[]{1, 3, 5, 2, 4, 6}, 2, 3));

        MatrixMultiplicationVertex N = m.matrixMultiply(alpha);
        PartialDerivatives NDiff = N.getDerivativeWrtLatents();

        PartialDerivatives reverseModePartialDiff = Differentiator.reverseModeAutoDiff(N, new HashSet<>(Arrays.asList(m, alpha)));

        DoubleTensor dNdmForward = NDiff.withRespectTo(m);
        DoubleTensor dNdmReverse = reverseModePartialDiff.withRespectTo(m);
        DoubleTensor expectedDNdm = DoubleTensor.create(new double[]{1, 2, 3, 4, 5, 6}, 1, 3, 1, 2);

        assertEquals(expectedDNdm, dNdmForward);
        assertEquals(expectedDNdm, dNdmReverse);

        DoubleTensor dNdAlphaForward = NDiff.withRespectTo(alpha);
        DoubleTensor dNdAlphaReverse = reverseModePartialDiff.withRespectTo(alpha);
        DoubleTensor expectedDNdAlpha = DoubleTensor.create(new double[]{
            1, 0, 0,
            2, 0, 0,
            0, 1, 0,
            0, 2, 0,
            0, 0, 1,
            0, 0, 2
        }, 1, 3, 2, 3);

        assertEquals(expectedDNdAlpha, dNdAlphaForward);
        assertEquals(expectedDNdAlpha, dNdAlphaReverse);
    }

    @Test
    public void canDoDoubleMatrixMultiplyAutoDiff() {

        DoubleVertex m = new UniformVertex(0, 10);
        m.setValue(DoubleTensor.create(new double[]{
            1, 2
        }, 1, 2));

        DoubleVertex alpha = new UniformVertex(0, 10);
        alpha.setValue(DoubleTensor.create(new double[]{
            1, 3,
            2, 4
        }, 2, 2));

        DoubleVertex beta = new UniformVertex(0, 10);
        beta.setValue(DoubleTensor.create(new double[]{
            5, 7,
            6, 8
        }, 2, 2));

        DoubleVertex N = m.matrixMultiply(alpha);
        MatrixMultiplicationVertex y = N.matrixMultiply(beta);

        PartialDerivatives yDiff = y.getDerivativeWrtLatents();
        PartialDerivatives dydx = Differentiator.reverseModeAutoDiff(y, m, alpha, beta);

        DoubleTensor dydmForward = yDiff.withRespectTo(m);
        DoubleTensor dydmReverse = dydx.withRespectTo(m);
        DoubleTensor expectedDydm = DoubleTensor.create(new double[]{
            23, 34, 31, 46
        }, 1, 2, 1, 2);

        assertEquals(expectedDydm, dydmForward);
        assertEquals(expectedDydm, dydmReverse);

        DoubleTensor dydalphaForward = yDiff.withRespectTo(alpha);
        DoubleTensor dydalphaReverse = dydx.withRespectTo(alpha);
        DoubleTensor expectedDydalpha = DoubleTensor.create(new double[]{
            5, 6,
            10, 12,
            7, 8,
            14, 16
        }, 1, 2, 2, 2);

        assertEquals(expectedDydalpha, dydalphaForward);
        assertEquals(expectedDydalpha, dydalphaReverse);

        DoubleTensor dydbetaForward = yDiff.withRespectTo(beta);
        DoubleTensor dydbetaReverse = dydx.withRespectTo(beta);
        DoubleTensor expectedDydbeta = DoubleTensor.create(new double[]{
            5, 0,
            11, 0,
            0, 5,
            0, 11
        }, 1, 2, 2, 2);

        assertEquals(expectedDydbeta, dydbetaForward);
        assertEquals(expectedDydbeta, dydbetaReverse);
    }

    @Test
    public void canDoTripleMatrixMultiplyAutoDiff() {

        DoubleVertex m = new UniformVertex(0, 10);
        m.setValue(DoubleTensor.create(new double[]{
            1,
            2
        }, 2, 1));

        DoubleVertex alpha = new UniformVertex(0, 10);
        alpha.setValue(DoubleTensor.create(new double[]{
            1, 3,
            2, 4
        }, 2, 2));

        DoubleVertex beta = new UniformVertex(0, 10);
        beta.setValue(DoubleTensor.create(new double[]{
            5, 8,
            6, 9,
            7, 10
        }, 3, 2));

        DoubleVertex N = alpha.matrixMultiply(m);
        DoubleVertex L = beta.matrixMultiply(alpha);
        //y = L x N = (beta x alpha) x (alpha x m)
        MatrixMultiplicationVertex y = L.matrixMultiply(N);
        PartialDerivatives yDiff = y.getDerivativeWrtLatents();
        PartialDerivatives dydx = Differentiator.reverseModeAutoDiff(y, new HashSet<>(Arrays.asList(alpha)));

        DoubleTensor dydalphaForward = yDiff.withRespectTo(alpha);
        DoubleTensor dydalphaReverse = dydx.withRespectTo(alpha);
        DoubleTensor expectedDydalpha = DoubleTensor.create(new double[]{
            56, 92,
            103, 174,
            66, 108,
            117, 198,
            76, 124,
            131, 222
        }, 3, 1, 2, 2);

        assertEquals(expectedDydalpha, dydalphaForward);
        assertEquals(expectedDydalpha, dydalphaReverse);
    }

    @Test
    public void changesMatchGradient() {
        DoubleVertex inputA = new UniformVertex(new long[]{2, 5}, -10.0, 10.0);
        DoubleVertex inputB = new UniformVertex(new long[]{5, 4}, -10.0, 10.0);
        MatrixMultiplicationVertex outputVertex = inputA.matrixMultiply(inputB);
        final double INCREMENT = 10;
        final double DELTA = 1e-10;

        finiteDifferenceMatchesForwardAndReverseModeGradient(ImmutableList.of(inputA, inputB), outputVertex, INCREMENT, DELTA);
    }

}
