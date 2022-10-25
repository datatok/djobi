package io.datatok.djobi.loaders.matrix;

import io.datatok.djobi.engine.Parameter;
import io.datatok.djobi.utils.bags.ParameterBag;

import java.util.List;

public interface IMatrixGenerator {
    List<ParameterBag> generate(final Parameter parentParameter) throws Exception;
}
