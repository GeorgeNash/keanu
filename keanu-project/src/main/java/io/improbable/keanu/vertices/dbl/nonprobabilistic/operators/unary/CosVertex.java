package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.unary;

import io.improbable.keanu.annotation.ExportVertexToPythonBindings;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.LoadVertexParam;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.Differentiable;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivative;

import java.util.HashMap;
import java.util.Map;

public class CosVertex extends DoubleUnaryOpVertex implements Differentiable {

    /**
     * Takes the cosine of a vertex, Cos(vertex)
     *
     * @param inputVertex the vertex
     */
    @ExportVertexToPythonBindings
    public CosVertex(@LoadVertexParam(INPUT_VERTEX_NAME) DoubleVertex inputVertex) {
        super(inputVertex);
    }

    @Override
    protected DoubleTensor op(DoubleTensor value) {
        return value.cos();
    }

    @Override
    public PartialDerivative forwardModeAutoDifferentiation(Map<Vertex, PartialDerivative> derivativeOfParentsWithRespectToInputs) {
        PartialDerivative derivativeOfParentWithRespectToInputs = derivativeOfParentsWithRespectToInputs.get(inputVertex);
        DoubleTensor inputValue = inputVertex.getValue();

        DoubleTensor dCos = inputValue.sin().unaryMinusInPlace();
        return derivativeOfParentWithRespectToInputs.multiplyAlongOfDimensions(dCos, inputValue.getShape());
    }

    @Override
    public Map<Vertex, PartialDerivative> reverseModeAutoDifferentiation(PartialDerivative derivativeOfOutputsWithRespectToSelf) {
        Map<Vertex, PartialDerivative> partials = new HashMap<>();
        partials.put(inputVertex, derivativeOfOutputsWithRespectToSelf
            .multiplyAlongWrtDimensions(inputVertex.getValue().sin().unaryMinusInPlace(), this.getShape()));
        return partials;
    }
}
