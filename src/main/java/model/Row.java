package model;

import java.util.List;

public record Row(List<Object> values) {
    public int size() {
        return values.size();
    }
    public Object get(int index) {
        return values.get(index);
    }
}
