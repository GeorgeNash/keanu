# Stubs for py4j.tests.py4j_signals_test (Python 3.6)
#
# NOTE: This dynamically typed stub was automatically generated by stubgen.

import unittest
from typing import Any

class MockListener:
    test_case: Any = ...
    received: Any = ...
    def __init__(self, test_case: Any) -> None: ...
    def started(self, sender: Any, **kwargs: Any) -> None: ...
    def connection_started(self, sender: Any, **kwargs: Any) -> None: ...
    def connection_stopped(self, sender: Any, **kwargs: Any) -> None: ...
    def stopped(self, sender: Any, **kwargs: Any) -> None: ...
    def pre_shutdown(self, sender: Any, **kwargs: Any) -> None: ...
    def post_shutdown(self, sender: Any, **kwargs: Any) -> None: ...

class JavaGatewayTest(unittest.TestCase):
    def test_all_regular_signals_auto_start(self) -> None: ...

class ClientServerTest(unittest.TestCase):
    def test_all_regular_signals(self) -> None: ...
    def test_signals_started_from_python(self) -> None: ...
