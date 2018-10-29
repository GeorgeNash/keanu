"""
Some utility functions to compare tensor-like things
They might be Keanu tensors or Numpy arrays
"""
def tensors_equal(t1, t2):
    if not shapes_equal(__get_shape(t1), __get_shape(t2)):
        print("Shapes don't match: %s != %s" % (__as_list(__get_shape(t1)), __as_list(__get_shape(t2))))
        return False
    it1 = __as_iterator(t1)
    it2 = __as_iterator(t2)
    for idx, value in enumerate(zip(it1, it2)):
        if value[0] != value[1]:
            print("Mismatch at position %d: %s != %s" % (idx, value[0], value[1]))
            return False
    return True

def shapes_equal(s1, s2):
    if __get_length(s1) != __get_length(s2):
        print("Ranks don't match: %s vs %s" % (__as_list(s1), __as_list(s2)))
        return False
    for i in range(len(s2)):
        if s1[i] != s2[i]:
            print("Mismatch in dimension %d: %d != %d" % (i, s1[i], s2[i]))
            return False
    return True

def __get_shape(t):
    try:
        return t.getShape()
    except:
        return t.shape

def __get_length(t):
    try:
        return len(t)
    except:
        return len(__as_iterator(t))

def __as_iterator(t):
    try:
        return (i for i in t.asFlatArray())
    except:
        return (i for i in t.flatten())

def __as_list(t):
    return list(__as_iterator(t))