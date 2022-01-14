package io.datatok.djobi.engine.data;

import java.util.List;

public class ArrayData<V> extends DataHolder {

    public int length;

    public List<V> elements;

    private Class<?> elementClass;

    public ArrayData(List<V> elements)
    {
        super(elements);

        this.type = "array";

        this.elements = elements;
        this.length = elements.size();

        this.elementClass = elements.size() > 0 ? elements.get(0).getClass() : null;
    }

    public Class<?> getElementClass() {
        return elementClass;
    }

    public List<V> getData() {
        return this.elements;
    }

}
