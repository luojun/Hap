package ca.dragonflystudios.endu;

import android.database.Cursor;

/**
 * Created by Jun Luo on 13-12-02.
 */
public class ListEndu extends Endu {

    private Cursor mCursor;
    // TODO: current item ... 

    public void updateCursor(Cursor cursor) {
        mCursor = cursor;
        // TODO: replay ... point cursor to the first ... play first Endu ...
    }

}
