package net.amunak

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate

object RouteGenerator: Function {
	val routeList: Map<String, String> = mapOf(
		"configure" to "/configure/{clientId}",
		"configure.log_stream" to "/configure/{clientId}/log-stream",
		"control.ws" to "/control/{linkId}/ws",
	)

	fun generateRoute(route: String, args: Map<String, Any> = mapOf()): String {
		val routeTemplate = routeList[route] ?: throw IllegalArgumentException("Route $route not found")

		return routeTemplate.replace(Regex("\\{([^}]+)}")) {
			val argName = it.groupValues[1]
			val argValue = args[argName] ?: throw IllegalArgumentException("Missing argument $argName")

			argValue.toString()
		}
	}

	override fun getArgumentNames(): MutableList<String> {
		val argumentNames = mutableListOf<String>()
		argumentNames.add("route")
		argumentNames.add("args")

		return argumentNames
	}

	override fun execute(p0: MutableMap<String, Any>?, p1: PebbleTemplate?, p2: EvaluationContext?, p3: Int): Any {
		val route = p0?.get("route") as? String ?: throw IllegalArgumentException("Missing Route argument")
		val args = p0["args"] as? Map<String, Any> ?: mapOf<String, Any>()

		return generateRoute(route, args)
	}
}
