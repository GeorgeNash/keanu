from keanu.cast import (cast_tensor_arg_to_double, cast_tensor_arg_to_integer, cast_tensor_arg_to_bool, cast_to_double_vertex,
                        cast_to_integer_vertex, cast_to_bool_vertex)
from keanu.vartypes import (primitive_types, numpy_types, pandas_types)
import pytest
import numpy as np
import pandas as pd
from typing import Union, Callable
from keanu.vertex import Gaussian


@pytest.mark.parametrize("value", [1, 1., True])
@pytest.mark.parametrize("cast_fn, expected_type", [(cast_tensor_arg_to_double, float), (cast_to_double_vertex, float),
                                                    (cast_tensor_arg_to_integer, int), (cast_to_integer_vertex, int),
                                                    (cast_tensor_arg_to_bool, bool), (cast_to_bool_vertex, bool)])
def test_scalar_cast(value: primitive_types, cast_fn: Callable, expected_type: type) -> None:
    assert type(cast_fn(value)) == expected_type


@pytest.mark.parametrize("value", [
    np.array([1]),
    np.array([1.]),
    np.array([True]),
    np.array([[[1]]]),
    np.array([[1, 4], [5, 38]]),
    pd.DataFrame(data=[1]),
    pd.DataFrame(data=[1.]),
    pd.DataFrame(data=[True]),
    pd.DataFrame(data=[[1, 2], [4, 5]]),
    pd.Series(data=[1]),
    pd.Series(data=[1.]),
    pd.Series(data=[True]),
    pd.Series(data=[1, 3, 4]),
])
@pytest.mark.parametrize("cast_fn, expected_type", [(cast_tensor_arg_to_double, np.floating),
                                                    (cast_to_double_vertex, np.floating),
                                                    (cast_tensor_arg_to_integer, np.integer),
                                                    (cast_to_integer_vertex, np.integer), (cast_tensor_arg_to_bool, np.bool_),
                                                    (cast_to_bool_vertex, np.bool_)])
def test_nonscalar_cast(value: Union[numpy_types, pandas_types], cast_fn: Callable, expected_type: type) -> None:
    assert cast_fn(value).dtype == expected_type


@pytest.mark.parametrize("cast_fn, cast_to_type", [(cast_tensor_arg_to_double, float),
                                                   (cast_tensor_arg_to_integer, int), (cast_tensor_arg_to_bool, bool)])
def test_cant_pass_vertex_to_cast_tensor_arg(cast_fn: Callable, cast_to_type: type) -> None:
    gaussian = Gaussian(0., 1.)

    with pytest.raises(TypeError, match="Cannot cast {} to {}".format(type(gaussian), cast_to_type)):
        cast_fn(gaussian)


@pytest.mark.parametrize("cast_fn", [cast_to_double_vertex, cast_to_integer_vertex, cast_to_bool_vertex])
def test_can_pass_vertex_to_cast(cast_fn: Callable) -> None:
    gaussian = Gaussian(0., 1.)
    assert type(cast_fn(gaussian)) == type(gaussian)
