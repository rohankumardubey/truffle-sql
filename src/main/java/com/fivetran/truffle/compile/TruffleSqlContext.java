package com.fivetran.truffle.compile;

import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.Layout;
import com.oracle.truffle.api.object.Shape;

import java.io.*;

public class TruffleSqlContext extends ExecutionContext {
    public final BufferedReader in;
    public final PrintWriter out, err;

    static TruffleSqlContext fromEnv(TruffleLanguage.Env env) {
        return new TruffleSqlContext(env.in(), env.out(), env.err());
    }

    static TruffleSqlContext fromStreams(InputStream in, OutputStream out, OutputStream err) {
        return new TruffleSqlContext(in, out, err);
    }

    private TruffleSqlContext(InputStream in, OutputStream out, OutputStream err) {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = new PrintWriter(out, true);
        this.err = new PrintWriter(err, true);
    }

    /**
     * Indicates that an object was generated by Truffle-SQL
     */
    static final Layout LAYOUT = Layout.createLayout();

    static final Shape EMPTY = LAYOUT.createShape(SqlObjectType.INSTANCE);

    static boolean isSqlObject(TruffleObject value) {
        return LAYOUT.getType().isInstance(value) && LAYOUT.getType().cast(value).getShape().getObjectType() == SqlObjectType.INSTANCE;
    }
}
