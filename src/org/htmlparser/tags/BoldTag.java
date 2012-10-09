package org.htmlparser.tags;

public class BoldTag extends CompositeTag
{
    private static final String[] mIds = new String[] {"B","STRONG"};
    public BoldTag ()
    {
    }
    public String[] getIds ()
    {
        return (mIds);
    }
    public String[] getEnders ()
    {
        return (mIds);
    }
    public String[] getEndTagEnders ()
    {
        return (new String[0]);
    }
}
