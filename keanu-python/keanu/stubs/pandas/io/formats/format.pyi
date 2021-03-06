# Stubs for pandas.io.formats.format (Python 3.6)
#
# NOTE: This dynamically typed stub was automatically generated by stubgen.

from typing import Any, Optional

common_docstring: str
return_docstring: str
docstring_to_string: Any

class CategoricalFormatter:
    categorical: Any = ...
    buf: Any = ...
    na_rep: Any = ...
    length: Any = ...
    footer: Any = ...
    def __init__(self, categorical: Any, buf: Optional[Any] = ..., length: bool = ..., na_rep: str = ..., footer: bool = ...) -> None: ...
    def to_string(self): ...

class SeriesFormatter:
    series: Any = ...
    buf: Any = ...
    name: Any = ...
    na_rep: Any = ...
    header: Any = ...
    length: Any = ...
    index: Any = ...
    max_rows: Any = ...
    float_format: Any = ...
    dtype: Any = ...
    adj: Any = ...
    def __init__(self, series: Any, buf: Optional[Any] = ..., length: bool = ..., header: bool = ..., index: bool = ..., na_rep: str = ..., name: bool = ..., float_format: Optional[Any] = ..., dtype: bool = ..., max_rows: Optional[Any] = ...) -> None: ...
    def to_string(self): ...

class TextAdjustment:
    encoding: Any = ...
    def __init__(self) -> None: ...
    def len(self, text: Any): ...
    def justify(self, texts: Any, max_len: Any, mode: str = ...): ...
    def adjoin(self, space: Any, *lists: Any, **kwargs: Any): ...

class EastAsianTextAdjustment(TextAdjustment):
    ambiguous_width: int = ...
    def __init__(self) -> None: ...
    def len(self, text: Any): ...
    def justify(self, texts: Any, max_len: Any, mode: str = ...): ...

class TableFormatter:
    is_truncated: bool = ...
    show_dimensions: Any = ...
    @property
    def should_show_dimensions(self): ...

class DataFrameFormatter(TableFormatter):
    __doc__: Any = ...
    frame: Any = ...
    buf: Any = ...
    show_index_names: Any = ...
    sparsify: Any = ...
    float_format: Any = ...
    formatters: Any = ...
    na_rep: Any = ...
    decimal: Any = ...
    col_space: Any = ...
    header: Any = ...
    index: Any = ...
    line_width: Any = ...
    max_rows: Any = ...
    max_cols: Any = ...
    max_rows_displayed: Any = ...
    show_dimensions: Any = ...
    table_id: Any = ...
    justify: Any = ...
    kwds: Any = ...
    columns: Any = ...
    adj: Any = ...
    def __init__(self, frame: Any, buf: Optional[Any] = ..., columns: Optional[Any] = ..., col_space: Optional[Any] = ..., header: bool = ..., index: bool = ..., na_rep: str = ..., formatters: Optional[Any] = ..., justify: Optional[Any] = ..., float_format: Optional[Any] = ..., sparsify: Optional[Any] = ..., index_names: bool = ..., line_width: Optional[Any] = ..., max_rows: Optional[Any] = ..., max_cols: Optional[Any] = ..., show_dimensions: bool = ..., decimal: str = ..., table_id: Optional[Any] = ..., **kwds: Any) -> None: ...
    max_cols_adj: Any = ...
    def to_string(self) -> None: ...
    def to_latex(self, column_format: Optional[Any] = ..., longtable: bool = ..., encoding: Optional[Any] = ..., multicolumn: bool = ..., multicolumn_format: Optional[Any] = ..., multirow: bool = ...) -> None: ...
    def to_html(self, classes: Optional[Any] = ..., notebook: bool = ..., border: Optional[Any] = ...) -> None: ...
    @property
    def has_index_names(self): ...
    @property
    def has_column_names(self): ...

def format_array(values: Any, formatter: Any, float_format: Optional[Any] = ..., na_rep: str = ..., digits: Optional[Any] = ..., space: Optional[Any] = ..., justify: str = ..., decimal: str = ...): ...

class GenericArrayFormatter:
    values: Any = ...
    digits: Any = ...
    na_rep: Any = ...
    space: Any = ...
    formatter: Any = ...
    float_format: Any = ...
    justify: Any = ...
    decimal: Any = ...
    quoting: Any = ...
    fixed_width: Any = ...
    def __init__(self, values: Any, digits: int = ..., formatter: Optional[Any] = ..., na_rep: str = ..., space: int = ..., float_format: Optional[Any] = ..., justify: str = ..., decimal: str = ..., quoting: Optional[Any] = ..., fixed_width: bool = ...) -> None: ...
    def get_result(self): ...

class FloatArrayFormatter(GenericArrayFormatter):
    formatter: Any = ...
    float_format: Any = ...
    def __init__(self, *args: Any, **kwargs: Any) -> None: ...
    def get_result_as_array(self): ...

class IntArrayFormatter(GenericArrayFormatter): ...

class Datetime64Formatter(GenericArrayFormatter):
    nat_rep: Any = ...
    date_format: Any = ...
    def __init__(self, values: Any, nat_rep: str = ..., date_format: Optional[Any] = ..., **kwargs: Any) -> None: ...

class IntervalArrayFormatter(GenericArrayFormatter):
    def __init__(self, values: Any, *args: Any, **kwargs: Any) -> None: ...

class PeriodArrayFormatter(IntArrayFormatter): ...

class CategoricalArrayFormatter(GenericArrayFormatter):
    def __init__(self, values: Any, *args: Any, **kwargs: Any) -> None: ...

def format_percentiles(percentiles: Any): ...

class Datetime64TZFormatter(Datetime64Formatter): ...

class Timedelta64Formatter(GenericArrayFormatter):
    nat_rep: Any = ...
    box: Any = ...
    def __init__(self, values: Any, nat_rep: str = ..., box: bool = ..., **kwargs: Any) -> None: ...

class EngFormatter:
    ENG_PREFIXES: Any = ...
    accuracy: Any = ...
    use_eng_prefix: Any = ...
    def __init__(self, accuracy: Optional[Any] = ..., use_eng_prefix: bool = ...) -> None: ...
    def __call__(self, num: Any): ...

def set_eng_float_format(accuracy: int = ..., use_eng_prefix: bool = ...) -> None: ...
def get_level_lengths(levels: Any, sentinel: str = ...): ...
def buffer_put_lines(buf: Any, lines: Any) -> None: ...
