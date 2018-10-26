import keanu as kn
import pytest
from py4j.java_gateway import java_import

@pytest.fixture
def java_list_wrapper():
    class JavaListWrapper(kn.JavaObjectWrapper):
        def __init__(self, values):
            super(JavaListWrapper, self).__init__(kn.KeanuContext().to_java_list(values))

        def index_of(self, value):
            return 100

    return JavaListWrapper([1, 2, 3])

def test_java_object_wrapper_cant_call_java_api_with_no_python_impl_if_camel_case(java_list_wrapper):
    with pytest.raises(AttributeError):
        assert not java_list_wrapper.isEmpty()

def test_java_object_wrapper_cant_call_java_api_with_python_impl_if_camel_case(java_list_wrapper):
    with pytest.raises(AttributeError):
        assert java_list_wrapper.indexOf(1) == 100

def test_java_object_wrapper_can_call_java_api_with_no_python_impl_if_snake_case(java_list_wrapper):
    assert not java_list_wrapper.is_empty()

def test_java_object_wrapper_can_call_java_api_with_no_python_impl_if_both_camel_case_and_snake_case(java_list_wrapper):
    assert java_list_wrapper.get(0) == 1

def test_java_object_wrapper_can_call_python_api(java_list_wrapper):
    assert java_list_wrapper.index_of(1) == 100