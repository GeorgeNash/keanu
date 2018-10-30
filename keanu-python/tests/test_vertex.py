import keanu as kn
import numpy as np
import pytest
from tests.keanu_assert import tensors_equal


@pytest.fixture
def jvm_view():
    from py4j.java_gateway import java_import
    jvm_view = kn.KeanuContext().jvm_view()
    java_import(jvm_view, "io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex")
    return jvm_view


def test_can_pass_scalar_to_vertex(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))
    sample = gaussian.sample()

    assert sample.isScalar()


def test_can_pass_ndarray_to_vertex(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (np.array([[0.1, 0.4]]), np.array([[0.4, 0.5]])))
    sample = gaussian.sample()

    shape = sample.getShape()
    assert sample.getRank() == 2
    assert shape[0] == 1
    assert shape[1] == 2


def test_use_vertex_as_hyperparameter_of_another_vertex(jvm_view):
    mu = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (mu, 1.))
    sample = gaussian.sample()

    assert sample.isScalar()


def test_can_pass_array_to_vertex(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, ([3, 3], 0., 1.))
    sample = gaussian.sample()

    shape = sample.getShape()
    assert sample.getRank() == 2
    assert shape[0] == 3
    assert shape[1] == 3


def test_cannot_pass_generic_to_vertex(jvm_view):
    class GenericExampleClass:
        pass

    with pytest.raises(ValueError) as excinfo:
        kn.Vertex(jvm_view.GaussianVertex, (GenericExampleClass(), GenericExampleClass()))

    assert str(excinfo.value) == "Can't parse generic argument. Was given {}".format(GenericExampleClass)


def test_vertex_can_observe_scalar(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))
    gaussian.observe(4.)

    assert gaussian.get_value().scalar() == 4.


def test_vertex_can_observe_ndarray(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))

    ndarray = np.array([[1.,2.]])
    gaussian.observe(ndarray)

    nd4j_tensor_flat = gaussian.get_value().asFlatArray()
    assert nd4j_tensor_flat[0] == 1.
    assert nd4j_tensor_flat[1] == 2.


def test_get_connected_graph(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))
    connected_graph = set(gaussian.get_connected_graph())

    assert len(connected_graph) == 3


def test_id_str_of_downstream_vertex_is_higher_than_upstream(jvm_view):
    hyper_params = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., hyper_params))

    hyper_params_id = hyper_params.get_id()
    gaussian_id = gaussian.get_id()

    assert type(hyper_params_id) == tuple
    assert type(gaussian_id) == tuple

    assert hyper_params_id < gaussian_id


def test_construct_vertex_with_java_vertex(jvm_view):
    java_vertex = kn.Vertex(jvm_view.GaussianVertex, (0., 1.)).unwrap()
    python_vertex = kn.Vertex(java_vertex)

    assert tuple(java_vertex.getId().getValue()) == python_vertex.get_id()


def test_java_collections_to_generator(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))

    java_collections = gaussian.unwrap().getConnectedGraph()
    python_list = list(kn.Vertex._to_generator(java_collections))

    java_vertex_ids = [kn.Vertex._get_python_id(java_vertex) for java_vertex in java_collections]

    assert java_collections.size() == len(python_list)
    assert all(type(element) == kn.Vertex and element.get_id() in java_vertex_ids for element in python_list)


def test_get_vertex_id(jvm_view):
    gaussian = kn.Vertex(jvm_view.GaussianVertex, (0., 1.))

    java_id = gaussian.unwrap().getId().getValue()
    python_id = gaussian.get_id()

    assert all(value in python_id for value in java_id)
