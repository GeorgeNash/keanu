import numpy as np
from examples import CoalMining
from keanu import BayesNet, KeanuRandom
from keanu.algorithm import sample


def test_coalmining():
    KeanuRandom.set_default_random_seed(1)
    coal_mining = CoalMining()
    model = coal_mining.model()

    model.disasters.observe(coal_mining.training_data())

    net = BayesNet(model.switchpoint.get_connected_graph())
    samples = sample(net=net, sample_from=net.get_latent_vertices(), draws=50000, drop=10000, down_sample_interval=5)

    vertex_samples = samples[model.switchpoint.get_id()]

    switch_year = np.argmax(np.bincount(vertex_samples))

    assert switch_year == 1890
