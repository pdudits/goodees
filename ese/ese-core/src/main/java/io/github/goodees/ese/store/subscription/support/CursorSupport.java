package io.github.goodees.ese.store.subscription.support;

import io.github.goodees.ese.store.subscription.Cursor;
import io.github.goodees.ese.store.subscription.ObservableStoreException;
import io.github.goodees.ese.store.subscription.SerializedCursor;

public abstract class CursorSupport<C extends Cursor<C>> {
    private Class<C> cursorClass;

    protected CursorSupport(Class<C> cursorClass) {
        this.cursorClass = cursorClass;
    }

    protected C parseCursor(Cursor<?> cursor) {
        if (cursor == null) {
            throw ObservableStoreException.invalidCursor(null, null);
        }
        if (cursorClass.isInstance(cursor)) {
            return cursorClass.cast(cursor);
        } else if (cursor instanceof SerializedCursor) {
            C parsedCursor = null;
            try {
                parsedCursor = parseCursor(cursor.toSerializedForm());
            } catch (Exception e) {
                throw ObservableStoreException.invalidCursor(cursor, e);
            }
            if (parsedCursor == null) {
                throw ObservableStoreException.implementationError("parseCursor returned null cursor for "+cursor);
            } else {
                return parsedCursor;
            }
        } else {
            throw ObservableStoreException.invalidCursorClass(cursor);
        }
    }

    protected abstract C parseCursor(String s) throws Exception;
}
