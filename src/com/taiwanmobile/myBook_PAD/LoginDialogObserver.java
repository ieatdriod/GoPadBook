package com.taiwanmobile.myBook_PAD;

public interface LoginDialogObserver {
	public static final int KErrNone = 0;
	public static final int KErrCancel = 1;
	public static final int KErrFailed= 2;
	public void LoginComplete(LoginDialogController aController,Object aUserData, int err);
}
