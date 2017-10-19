package ru.capjack.degos.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultDegosPublishExtensionTest {
	
	private DefaultDegosPublishExtension extension;
	
	@BeforeEach
	void init() {
		extension = new DefaultDegosPublishExtension(null, null);
	}
	
	@Test
	void defaultValueOfInternalIsFalse() {
		assertFalse(extension.isAsPrivate());
	}
	
	@Test
	void setInternalToTrue() {
		extension.setAsPrivate(true);
		assertTrue(extension.isAsPrivate());
	}
	
	@Test
	void changeUsernameValue() {
		extension.setUsername("user");
		assertEquals("user", extension.getUsername());
	}
	
	@Test
	void changePasswordValue() {
		extension.setPassword("pass");
		assertEquals("pass", extension.getPassword());
	}
}