package io.improbable.keanu.vertices.bool.nonprobabilistic.operators.unary;

import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import io.improbable.keanu.tensor.bool.BooleanTensor;
import io.improbable.keanu.vertices.LoadParentVertex;
import io.improbable.keanu.vertices.Vertex;

public class NotVertex extends BoolUnaryOpVertex<BooleanTensor> {

    @ExportVertexToPythonBindings
    public NotVertex(@LoadParentVertex(INPUT_NAME) Vertex<BooleanTensor> a) {
        super(a.getShape(), a);
    }

    @Override
    protected BooleanTensor op(BooleanTensor value) {
        return value.not();
    }
}
