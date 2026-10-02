package fr.nzosifou.agas.agent

import fr.nzosifou.agas.agent.api.AgentHost
import fr.nzosifou.agas.agent.api.LogLevel

/** Journal de l'Agent, affiché par le Manager (onglet Journal). */
internal object AgentLog {

    @Volatile
    var host: AgentHost? = null

    fun d(message: String) = host?.log(LogLevel.DEBUG, message)
    fun i(message: String) = host?.log(LogLevel.INFO, message)
    fun action(message: String) = host?.log(LogLevel.ACTION, message)
    fun w(message: String) = host?.log(LogLevel.WARN, message)
}
