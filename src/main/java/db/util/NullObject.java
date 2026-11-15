package db.util;

import java.io.Serializable;

public class NullObject implements Serializable {
    @Override
    public String toString() {
        return "NULL";
    }
}
