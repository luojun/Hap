package ca.dragonflystudios.navigation;

/**
 * Created by jun on 2014-04-25.
 */
public class OrderedTreeNavigable implements Navigable
{
    public enum OrderedTreeMove
    {
        GOTO_ROOT, GOTO_PARENT, GOTO_CHILD, GOTO_SIBLING, GOTO_PREVIOUS_SIBLING, GOTO_NEXT_SIBLING;

        public Navigable apply(Navigable navigable)
        {
            if (navigable instanceof OrderedTreeNavigable)
                throw new IllegalArgumentException("Expecting: " + OrderedTreeNavigable.class.getName());

            return null;
        }
    }
}
