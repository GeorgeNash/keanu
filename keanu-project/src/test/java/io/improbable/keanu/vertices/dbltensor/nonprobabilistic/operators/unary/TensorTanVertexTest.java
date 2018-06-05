package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.vertices.dbltensor.DoubleVertex;
import org.junit.Test;

import static io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary.UnaryOperationTestHelpers.*;

public class TensorTanVertexTest {

    @Test
    public void tanScalarVertexValue() {
        operatesOnScalarVertexValue(
            Math.PI,
            Math.tan(Math.PI),
            DoubleVertex::tan
        );
    }

    @Test
    public void calculatesDualNumberOScalarTan() {
        calculatesDualNumberOfScalar(
            0.5,
            1 / Math.pow(Math.cos(0.5), 2),
            DoubleVertex::tan
        );
    }

    @Test
    public void tanMatrixVertexValues() {
        operatesOn2x2MatrixVertexValues(
            new double[]{0.0, 0.1, 0.2, 0.3},
            new double[]{Math.tan(0.0), Math.tan(0.1), Math.tan(0.2), Math.tan(0.3)},
            DoubleVertex::tan
        );
    }

    @Test
    public void calculatesDualNumberOfMatrixElementWiseTan() {
        calculatesDualNumberOfMatrixElementWiseOperator(
            new double[]{0.1, 0.2, 0.3, 0.4},
            new double[]{1 / Math.pow(Math.cos(0.1), 2),
                1 / Math.pow(Math.cos(0.2), 2),
                1 / Math.pow(Math.cos(0.3), 2),
                1 / Math.pow(Math.cos(0.4), 2)
            },
            DoubleVertex::tan
        );
    }
    
}
