package ca.dragonflystudios.navigation;

import java.util.Stack;

/**
 *
 * Navigation management. Coordinate navigable with pilotable. Maps piloting maneuvers to navigation
 * moves. Update navigation state ("where am i?"). Record history.
 *
 */

public class Navigator
{
    public Navigator()
    {
        mUnwindStack = new Stack<Navigable>();
        mRewindStack = new Stack<Navigable>();
    }

    public enum NavigatorMove
    {
        NONE, REWIND, UNWIND;

        public boolean move(Navigator navigator)
        {
            switch (this)
            {
                case REWIND :
                    return navigator.rewind();
                case UNWIND :
                    return navigator.unwind();
                default :
                    return false;
            }
        }

        public static NavigatorMove map(Pilotable.Maneuver maneuver)
        {
            switch (maneuver) {
                case FORWARD :
                    return UNWIND;
                case BACKWARD :
                    return REWIND;
                default :
                    return NONE;
            }
        }
    }

    public boolean navigate(Pilotable.Maneuver maneuver)
    {
        NavigatorMove navigatorMove = NavigatorMove.map(maneuver);
        if (NavigatorMove.NONE != navigatorMove) {
            return navigatorMove.move(this);
        } else {
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
        }
        return true;
    }

    /**
     * Like undo
     * @return
     */
    public boolean rewind()
    {
        if (!mRewindStack.empty()) {
            mUnwindStack.push(mCurrentNavigable);
            mCurrentNavigable = mRewindStack.pop();
            return true;
        }
        return false;
    }

    /**
     * Like redo
     * @return
     */
    public boolean unwind()
    {
        if (!mUnwindStack.empty()) {
            mRewindStack.push(mCurrentNavigable);
            mCurrentNavigable = mUnwindStack.pop();
            return true;
        }
        return false;
    }

    private static OrderedTreeNavigable.OrderedTreeMove map(Pilotable.Maneuver maneuver)
    {
        switch (maneuver)
        {
            case LEFT :
                return OrderedTreeNavigable.OrderedTreeMove.GOTO_PREVIOUS_SIBLING;
            case RIGHT :
                return OrderedTreeNavigable.OrderedTreeMove.GOTO_NEXT_SIBLING;
            case UP :
                return OrderedTreeNavigable.OrderedTreeMove.GOTO_PARENT;
            case DOWN :
                return OrderedTreeNavigable.OrderedTreeMove.GOTO_CHILD;
            default :
                return null;
        }
    }

    private Stack<Navigable> mRewindStack;
    private Navigable mCurrentNavigable;
    private Stack<Navigable> mUnwindStack;
}
