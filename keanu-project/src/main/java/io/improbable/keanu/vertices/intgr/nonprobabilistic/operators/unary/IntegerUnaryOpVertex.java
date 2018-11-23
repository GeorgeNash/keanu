package io.improbable.keanu.vertices.intgr.nonprobabilistic.operators.unary;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.NonProbabilistic;
import io.improbable.keanu.vertices.NonSaveableVertex;
import io.improbable.keanu.vertices.SaveParentVertex;
import io.improbable.keanu.vertices.VertexUnaryOp;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public abstract class IntegerUnaryOpVertex extends IntegerVertex implements NonProbabilistic<IntegerTensor>, VertexUnaryOp<IntegerVertex>, NonSaveableVertex {

    protected final IntegerVertex inputVertex;
    protected static final String INPUT_NAME = "inputVertex";

    /**
     * A vertex that performs a user defined operation on a singe input vertex
     *
     * @param inputVertex the input vertex
     */
    public IntegerUnaryOpVertex(IntegerVertex inputVertex) {
        this(inputVertex.getShape(), inputVertex);
    }

    /**
     * A vertex that performs a user defined operation on a singe input vertex
     *
     * @param shape       the shape of the tensor
     * @param inputVertex the input vertex
     */
    public IntegerUnaryOpVertex(long[] shape, IntegerVertex inputVertex) {
        super(shape);
        this.inputVertex = inputVertex;
        setParents(inputVertex);
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return op(inputVertex.sample(random));
    }

    @Override
    public IntegerVertex getInput() {
        return inputVertex;
    }


    @Override
    public IntegerTensor calculate() {
        return op(inputVertex.getValue());
    }

    protected abstract IntegerTensor op(IntegerTensor value);

    @SaveParentVertex(INPUT_NAME)
    public IntegerVertex getInputVertex() {
        return inputVertex;
    }
}
