package com.fivetran.truffle;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.column.ColumnReadStore;
import org.apache.parquet.column.ColumnReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Type;

public abstract class ExprAssembleColumn extends ExprAssemble {
    private final PrimitiveType type;
    private final ColumnDescriptor column;
    private ColumnReader reader;
    private final int maxDefinitionLevel;

    public ExprAssembleColumn(MessageType root, String[] path) {
        this.type = (PrimitiveType) root.getType(path);
        this.column = root.getColumnDescription(path);
        this.maxDefinitionLevel = root.getMaxDefinitionLevel(path);
    }

    @Override
    @TruffleBoundary
    public void prepare(ColumnReadStore readStore) {
        this.reader = readStore.getColumnReader(column);
    }

    @Override
    @TruffleBoundary
    public long getTotalValueCount() {
        return reader.getTotalValueCount();
    }

    /**
     * If column is not nullable, we can just get its primitive value without checking the definition level.
     */
    @Specialization(guards = {"!isNullable()", "isBoolean()"})
    public boolean getBoolean() {
        return doGetBoolean(reader);
    }

    protected boolean isBoolean() {
        return type.getPrimitiveTypeName() == PrimitiveType.PrimitiveTypeName.BOOLEAN;
    }

    /**
     * If column is nullable, optimistically assume it is never null and try to get a primitive value.
     * If definitionLevel < maxDefinitionLevel, abandon this path.
     */
    @Specialization(guards = "isBoolean()", rewriteOn = NotDefinedException.class)
    public boolean tryGetBoolean() {
        if (isNull(reader))
            throw new NotDefinedException();
        else
            return doGetBoolean(reader);
    }

    @TruffleBoundary
    private static boolean doGetBoolean(ColumnReader reader) {
        boolean result = reader.getBoolean();

        reader.consume();

        return result;
    }

    /**
     * Slow path, returns boxed values.
     */
    @Specialization(guards = {"isNullable()", "isBoolean()"})
    public Object getNullableBoolean() {
        if (isNull(reader))
            return doGetNull(reader);
        else
            return doGetBoolean(reader);
    }

    /**
     * If column is not nullable, we can just get its primitive value without checking the definition level.
     */
    @Specialization(guards = {"!isNullable()", "isDouble()"})
    public double getDouble() {
        return doGetDouble(reader);
    }

    protected boolean isDouble() {
        switch (type.getPrimitiveTypeName()) {
            case FLOAT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * If column is nullable, optimistically assume it is never null and try to get a primitive value.
     * If definitionLevel < maxDefinitionLevel, abandon this path.
     */
    @Specialization(guards = "isDouble()", rewriteOn = NotDefinedException.class)
    public double tryGetDouble() {
        if (isNull(reader))
            throw new NotDefinedException();
        else
            return doGetDouble(reader);
    }

    @TruffleBoundary
    private static double doGetDouble(ColumnReader reader) {
        double result = reader.getDouble();

        reader.consume();

        return result;
    }

    /**
     * Slow path, returns boxed values.
     */
    @Specialization(guards = {"isNullable()", "isDouble()"})
    public Object getNullableDouble() {
        if (isNull(reader))
            return doGetNull(reader);
        else
            return doGetDouble(reader);
    }

    /**
     * If column is not nullable, we can just get its primitive value without checking the definition level.
     */
    @Specialization(guards = {"!isNullable()", "isLong()"})
    public long getLong() {
        return doGetLong(reader);
    }

    @TruffleBoundary
    private static long doGetLong(ColumnReader reader) {
        long result = reader.getLong();

        reader.consume();

        return result;
    }

    protected boolean isLong() {
        switch (type.getPrimitiveTypeName()) {
            case INT32:
            case INT64:
                return true;
            default:
                return false;
        }
    }

    /**
     * If column is nullable, optimistically assume it is never null and try to get a primitive value.
     * If definitionLevel < maxDefinitionLevel, abandon this path.
     */
    @Specialization(guards = "isLong()", rewriteOn = NotDefinedException.class)
    public long tryGetLong() {
        if (isNull(reader))
            throw new NotDefinedException();
        else
            return doGetLong(reader);
    }

    /**
     * Slow path, returns boxed values.
     */
    @Specialization(guards = {"isNullable()", "isLong()"})
    public Object getNullableLong() {
        if (isNull(reader))
            return doGetNull(reader);
        else
            return doGetLong(reader);
    }

    /**
     * Slow path, returns boxed values.
     */
    @Specialization(guards = {"isString()"})
    public Object getNullableString() {
        if (isNull(reader))
            return doGetNull(reader);
        else
            return doGetString(reader);
    }

    @TruffleBoundary
    private static String doGetString(ColumnReader reader) {
        String result = reader.getBinary().toStringUsingUTF8();

        reader.consume();

        return result;
    }

    @TruffleBoundary
    private static SqlNull doGetNull(ColumnReader reader) {
        reader.consume();

        return SqlNull.INSTANCE;
    }

    protected boolean isString() {
        return type.getPrimitiveTypeName() == PrimitiveType.PrimitiveTypeName.BINARY;
    }

    @TruffleBoundary
    protected boolean isNull(ColumnReader reader) {
        return reader.getCurrentDefinitionLevel() < maxDefinitionLevel;
    }

    protected boolean isNullable() {
        return type.getRepetition() == Type.Repetition.OPTIONAL;
    }
}
