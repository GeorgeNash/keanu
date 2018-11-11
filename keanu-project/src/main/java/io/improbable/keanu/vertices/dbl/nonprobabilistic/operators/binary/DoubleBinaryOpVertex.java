package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;


import io.improbable.keanu.distributions.hyperparam.Diffs;
import io.improbable.keanu.distributions.hyperparam.ParameterName;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.NonProbabilistic;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkHasSingleNonScalarShapeOrAllScalar;

public abstract class DoubleBinaryOpVertex extends DoubleVertex implements NonProbabilistic<DoubleTensor> {

    protected final DoubleVertex left;
    protected final DoubleVertex right;

    /**
     * A vertex that performs a user defined operation on two vertices
     *
     * @param left  a vertex
     * @param right a vertex
     */
    public DoubleBinaryOpVertex(
        DoubleVertex left, DoubleVertex right) {
        this(checkHasSingleNonScalarShapeOrAllScalar(left.getShape(), right.getShape()),
            left, right);
    }

    /**
     * A vertex that performs a user defined operation on two vertices
     *
     * @param shape the shape of the resulting vertex
     * @param left  a vertex
     * @param right a vertex
     */
    public DoubleBinaryOpVertex(long[] shape, DoubleVertex left, DoubleVertex right) {
        super(shape);
        this.left = left;
        this.right = right;
        setParents(left, right);
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return op(left.sample(random), right.sample(random));
    }

    @Override
    public DoubleTensor calculate() {
        return op(left.getValue(), right.getValue());
    }

    public DoubleVertex getLeft() {
        return left;
    }

    public DoubleVertex getRight() {
        return right;
    }


    @Override
    public PartialDerivatives forwardModeAutoDifferentiation(Map<Vertex, PartialDerivatives> derivativeOfParentsWithRespectToInputs) {
        try {
            return forwardModeAutoDifferentiation(derivativeOfParentsWithRespectToInputs.get(left), derivativeOfParentsWithRespectToInputs.get(right));
        } catch (UnsupportedOperationException e) {
            return super.forwardModeAutoDifferentiation(derivativeOfParentsWithRespectToInputs);
        }
    }

    @Override
    public Map<ParameterName, Vertex> getParentsMap() {
        Map<ParameterName, Vertex> parentsMap = new LinkedHashMap<>();
        parentsMap.put(Diffs.LEFT, left);
        parentsMap.put(Diffs.RIGHT, right);

        return parentsMap;
    }

    protected abstract DoubleTensor op(DoubleTensor l, DoubleTensor r);

    protected abstract PartialDerivatives forwardModeAutoDifferentiation(PartialDerivatives l, PartialDerivatives r);
}
