package ca.dragonflystudios.navigation;

/**
 * Created by jun on 2014-04-25.
 */
public abstract class OrderedTreeNavigable implements Navigable
{
    public enum OrderedTreeMove
    {
        GOTO_ROOT, GOTO_PARENT, GOTO_CHILD, GOTO_SIBLING, GOTO_PREVIOUS_SIBLING, GOTO_NEXT_SIBLING;

        public Navigable apply(Navigable navigable)
        {
            if (navigable instanceof OrderedTreeNavigable)
                throw new IllegalArgumentException("Expecting: " + OrderedTreeNavigable.class.getName());
            OrderedTreeNavigable otn = (OrderedTreeNavigable) navigable;

            switch (this)
            {
                case GOTO_ROOT:
                    return otn.gotoRoot();
                case GOTO_PARENT:
                    return otn.gotoParent();
                case GOTO_CHILD:
                    return otn.gotoChild();
                case GOTO_SIBLING:
                    return otn.gotoSibling();
                case GOTO_PREVIOUS_SIBLING:
                    return otn.gotoPreviousSibling();
                case GOTO_NEXT_SIBLING:
                    return otn.gotoNextSibling();
            }
            return null;
        }
    }

    public abstract OrderedTreeNavigable gotoRoot();
    public abstract OrderedTreeNavigable gotoParent();
    public abstract OrderedTreeNavigable gotoChild();
    public abstract OrderedTreeNavigable gotoSibling();
    public abstract OrderedTreeNavigable gotoPreviousSibling();
    public abstract OrderedTreeNavigable gotoNextSibling();
}
