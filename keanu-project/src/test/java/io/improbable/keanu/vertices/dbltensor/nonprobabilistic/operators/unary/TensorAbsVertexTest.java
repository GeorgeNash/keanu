package io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary;

import io.improbable.keanu.vertices.dbltensor.DoubleVertex;
import org.junit.Test;

import static io.improbable.keanu.vertices.dbltensor.nonprobabilistic.operators.unary.UnaryOperationTestHelpers.*;

public class TensorAbsVertexTest {

    @Test
    public void absTwoScalarVertexValues() {
        operatesOnScalarVertexValue(
            -3,
            3.0,
            DoubleVertex::abs
        );
    }

    @Test
    public void absTwoMatrixVertexValues() {
        operatesOn2x2MatrixVertexValues(
            new double[]{-2, 3.0, -6.0, 4.0},
            new double[]{2.0, 3.0, 6.0, 4.0},
            DoubleVertex::abs
        );
    }

}
