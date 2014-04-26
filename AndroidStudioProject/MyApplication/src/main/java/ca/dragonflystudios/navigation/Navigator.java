package ca.dragonflystudios.navigation;

import java.util.Stack;

/**
 *
 * Navigation management. Links navigable with pilotable. Maps piloting maneuvers to navigation
 * moves. Update navigation state ("where am i?"). Record history.
 *
 */

public class Navigator
{
    public boolean navigate(Pilotable.Maneuver maneuver)
    {
        OrderedTreeNavigable.OrderedTreeMove move = map(maneuver);
        if (null == move)
            return false;

        Navigable navigable = move.apply(mCurrentNavigable);
        if (null == navigable)
            return false;

        // WAIL: thread safety
        mRewindStack.push(mCurrentNavigable);
        mCurrentNavigable = navigable;
        mUnwindStack.clear();

        return true;
    }

    public boolean rewind()
    {
        if (!mRewindStack.empty()) {
            mUnwindStack.push(mCurrentNavigable);
            mCurrentNavigable = mRewindStack.pop();
            return true;
        }
        return false;
    }

    public boolean unwind()
    {
        if (!mUnwindStack.empty()) {
            mRewindStack.push(mCurrentNavigable);
            mCurrentNavigable = mUnwindStack.pop();
            return true;
        }
        return false;
    }

    private OrderedTreeNavigable.OrderedTreeMove map(Pilotable.Maneuver maneuver)
    {
        switch (maneuver)
        {
            case FORWARD :

                break;
            case BACKWARD :
                break;
            case LEFT :
                break;
            case RIGHT :
                break;
            case UP :
                break;
            case DOWN :
                break;
        }

        return null;
    }

    private Stack<Navigable> mRewindStack;
    private Navigable mCurrentNavigable;
    private Stack<Navigable> mUnwindStack;
}
