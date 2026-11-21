package org.acme.demoapp.feature.example;

/** Planted: builds the guestbook's domain value from a form field without parse(). */
public class ExampleGuestShortcut {

	public static ExampleGuest fromForm(String name) {
		return new ExampleGuest(name);
	}
}
