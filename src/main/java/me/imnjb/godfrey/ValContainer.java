package me.imnjb.godfrey;

public class ValContainer<T> {

    private T value;

    public ValContainer() {
    }

    public ValContainer(T v) {
        this.value = v;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}