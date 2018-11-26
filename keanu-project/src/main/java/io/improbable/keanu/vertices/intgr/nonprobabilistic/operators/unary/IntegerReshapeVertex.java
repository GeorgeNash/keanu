package io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.NonSaveableVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public class IntegerReshapeVertex extends IntegerUnaryOpVertex implements NonSaveableVertex {
    public IntegerReshapeVertex(IntegerVertex inputVertex, long... proposedShape) {
        super(proposedShape, inputVertex);
    }

    @Override
    protected IntegerTensor op(IntegerTensor value) {
        return value.reshape(getShape());
    }
}
