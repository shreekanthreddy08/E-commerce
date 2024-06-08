package com.jsp.Shree_Myshop.helper;

import java.io.File;

public class AdminPassword {
	public static void main(String[] args) {
		File file = new File("src/main/resources/static/images");
		if (!file.isDirectory())
			file.mkdir();
	}
}
