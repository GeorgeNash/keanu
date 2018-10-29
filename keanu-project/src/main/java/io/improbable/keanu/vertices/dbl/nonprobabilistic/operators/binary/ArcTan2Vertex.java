package io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.PartialDerivatives;

import java.util.HashMap;
import java.util.Map;

public class ArcTan2Vertex extends DoubleBinaryOpVertex {

    /**
     * Calculates the signed angle, in radians, between the positive x-axis and a ray to the point (x, y) from the origin
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public ArcTan2Vertex(DoubleVertex x, DoubleVertex y) {
        super(x, y);
    }

    @Override
    protected DoubleTensor op(DoubleTensor x, DoubleTensor y) {
        return x.atan2(y);
    }

    @Override
    protected PartialDerivatives forwardModeAutoDifferentiation(PartialDerivatives dxWrtInputs, PartialDerivatives dyWrtInputs) {
        DoubleTensor yValue = right.getValue();
        DoubleTensor xValue = left.getValue();

        DoubleTensor denominator = yValue.pow(2).plusInPlace(xValue.pow(2));

        PartialDerivatives diffFromX = dxWrtInputs.multiplyAlongOfDimensions(
            yValue.div(denominator).unaryMinusInPlace(),
            xValue.getShape()
        );

        PartialDerivatives diffFromY = dyWrtInputs.multiplyAlongOfDimensions(
            xValue.div(denominator),
            yValue.getShape()
        );

        return diffFromX.add(diffFromY);
    }

    @Override
    public Map<Vertex, PartialDerivatives> reverseModeAutoDifferentiation(PartialDerivatives derivativeOfOutputsWithRespectToSelf) {
        Map<Vertex, PartialDerivatives> partials = new HashMap<>();
        DoubleTensor xValue = left.getValue();
        DoubleTensor yValue = right.getValue();

        DoubleTensor denominator = yValue.pow(2).plusInPlace(xValue.pow(2));
        DoubleTensor dOutWrtX = yValue.div(denominator).unaryMinusInPlace();
        DoubleTensor dOutWrtY = xValue.div(denominator);

        partials.put(left, derivativeOfOutputsWithRespectToSelf.multiplyAlongWrtDimensions(dOutWrtX, this.getShape()));
        partials.put(right, derivativeOfOutputsWithRespectToSelf.multiplyAlongWrtDimensions(dOutWrtY, this.getShape()));
        return partials;
    }
}
