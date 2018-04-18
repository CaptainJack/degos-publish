package ru.capjack.degos.publish

open class DegosPublishExtensionImpl(
	override var username: String,
	override var password: String
) : DegosPublishExtension {
	override var private: Boolean = false
}