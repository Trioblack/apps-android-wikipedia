package org.wikipedia.history;

import android.content.Context;
import org.wikipedia.data.ContentPersister;
import org.wikipedia.data.SQLiteContentProvider;

public class HistoryEntryPersister extends ContentPersister<HistoryEntry> {
    public HistoryEntryPersister(Context context) {
        // lolJava
        super(
                context.getContentResolver().acquireContentProviderClient(
                        SQLiteContentProvider.getAuthorityForTable(
                                HistoryEntry.PERSISTANCE_HELPER.getTableName()
                        )
                ),
                HistoryEntry.PERSISTANCE_HELPER
        );
    }
}
