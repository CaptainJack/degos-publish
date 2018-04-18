package ru.capjack.degos.publish

interface DegosPublishExtension {
	var username: String
	var password: String
	var private: Boolean
	
	companion object {
		const val NAME = "degosPublish"
	}
}
