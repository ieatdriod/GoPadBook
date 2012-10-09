package org.htmlparser.tags;

public class ItalicTag extends CompositeTag
{
    private static final String[] mIds = new String[] {"L","EM"};
    public ItalicTag ()
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
