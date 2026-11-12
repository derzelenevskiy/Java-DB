package storage;

import model.DataType;
import model.Row;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataSerializer {
    public void recordValue(DataOutputStream dos, DataType type, Object value) throws IOException {
        if (value == null) {
            dos.write(0);
            return;
        }
        dos.write(1);
        switch (type) {
            case INTEGER -> dos.writeInt((Integer) value);
            case DOUBLE -> dos.writeDouble((double) value);
            case STRING -> dos.writeUTF(String.valueOf(value));
            case BOOLEAN -> dos.writeBoolean((Boolean) value);
            case DATE -> dos.writeLong(((LocalDate) value).toEpochDay());
        }
    }

    public Object readValue(DataInputStream dis, DataType type) throws IOException {
        if (dis.readByte() == 0) {
            return null;
        }
        return switch (type) {
            case INTEGER -> dis.readInt();
            case DOUBLE -> dis.readDouble();
            case STRING -> dis.readUTF();
            case BOOLEAN -> dis.readBoolean();
            case DATE -> LocalDate.ofEpochDay(dis.readLong());
        };
    }

    public void serializeRow(DataOutputStream dos, List<DataType> rowTypes, Row row) throws IOException {
        if (rowTypes.size() != row.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < row.size(); ++i) {
            recordValue(dos, rowTypes.get(i), row.get(i));
        }
    }
    public Row deserializeRow(DataInputStream dis, List<DataType> rowTypes) throws IOException {
        ArrayList<Object> arr = new ArrayList<>();
        for (DataType i : rowTypes) {
            arr.add(readValue(dis, i));
        }
        return new Row(arr);
    }
}
