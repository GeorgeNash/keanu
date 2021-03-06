package io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.binary;

import io.improbable.keanu.annotation.DisplayInformationForOutput;
import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.LoadVertexParam;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

@DisplayInformationForOutput(displayName = "*")
public class IntegerMultiplicationVertex extends IntegerBinaryOpVertex {

    /**
     * Multiplies one vertex by another
     *
     * @param left a vertex to be multiplied
     * @param right a vertex to be multiplied
     */
    @ExportVertexToPythonBindings
    public IntegerMultiplicationVertex(@LoadVertexParam(LEFT_NAME) IntegerVertex left, @LoadVertexParam(RIGHT_NAME) IntegerVertex right) {
        super(left, right);
    }

    @Override
    protected IntegerTensor op(IntegerTensor l, IntegerTensor r) {
        return l.times(r);
    }
}
