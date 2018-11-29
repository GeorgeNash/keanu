# Stubs for pandas.io.formats.latex (Python 3.6)
#
# NOTE: This dynamically typed stub was automatically generated by stubgen.

from pandas.io.formats.format import TableFormatter
from typing import Any, Optional

class LatexFormatter(TableFormatter):
    fmt: Any = ...
    frame: Any = ...
    bold_rows: Any = ...
    column_format: Any = ...
    longtable: Any = ...
    multicolumn: Any = ...
    multicolumn_format: Any = ...
    multirow: Any = ...
    def __init__(self, formatter: Any, column_format: Optional[Any] = ..., longtable: bool = ..., multicolumn: bool = ..., multicolumn_format: Optional[Any] = ..., multirow: bool = ...) -> None: ...
    clinebuf: Any = ...
    def write_result(self, buf: Any): ...
