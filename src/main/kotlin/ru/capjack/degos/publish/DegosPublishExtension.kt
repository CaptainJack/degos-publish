package ru.capjack.degos.publish

open class DegosPublishExtension {
	var private: Boolean = false
	var username: String? = null
	var password: String? = null
	var publication: String? = null
	var publicationSources: PublicationSource = PublicationSource.ALWAYS
	
	enum class PublicationSource {
		ALWAYS,
		RELEASE,
		NEVER
	}
}
