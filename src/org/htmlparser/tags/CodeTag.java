package org.htmlparser.tags;

public class CodeTag extends CompositeTag
{
    private static final String[] mIds = new String[] {"CODE"};
    public CodeTag ()
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
