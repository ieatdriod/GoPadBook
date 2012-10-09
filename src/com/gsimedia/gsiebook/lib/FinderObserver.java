package com.gsimedia.gsiebook.lib;

import java.util.ArrayList;

public interface FinderObserver {
	static final int KErrNone = 0;
	static final int KErrNotFound = 1;
	public void NotifyFindComplete(int err, ArrayList<FindResult> aResult);
}
