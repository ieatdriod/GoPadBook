package tw.com.soyong.mebook;


//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Mebook
//  @ File Name : SyTree.java
//  @ Date : 2009/3/23
//  @ Author : Victor
//
//

public class SyTree<T> {
	public SyTreeNode<T> mRoot;
	
	public SyTree() {
	
	}
	
	public boolean isEmpty() {
		return (mRoot == null);
	}
	
	public void setRoot(SyTreeNode<T> root) {
		mRoot = root ;
	}
}
