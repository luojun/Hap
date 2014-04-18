package ca.dragonflystudios.hap;

public interface Pilotable
{
    public boolean up();

    public boolean down();

    public boolean next();

    public boolean previous();

    public Object getContent();

    public String getContentDescription();
}
