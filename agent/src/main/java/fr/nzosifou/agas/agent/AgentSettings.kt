package fr.nzosifou.agas.agent

import fr.nzosifou.agas.agent.api.AgentHost
import fr.nzosifou.agas.agent.api.AgentSetting

/**
 * Réglages de l'Agent, affichés par le Manager. Les clés sont mémorisées par le Manager : ne jamais
 * en renommer une (la valeur choisie par l'utilisateur serait perdue).
 */
internal object AgentSettings {

    const val HIJACK_GUARD = "hijack_guard"
    const val UNLABELED_BUTTONS = "unlabeled_buttons"
    const val WAKE_PLAYABLES = "wake_playables"
    const val STORE_EXIT_BUTTONS = "store_exit_buttons"
    const val BACK_FALLBACK = "back_fallback"
    const val SAVE_DUMPS = "save_dumps"

    private const val PROTECTION = "PROTECTION"
    private const val NO_REAL_CLOSE = "SANS VRAIE CROIX"
    private const val DIAGNOSTIC = "DIAGNOSTIC"

    val declared = listOf(
        AgentSetting(
            HIJACK_GUARD, PROTECTION, "Anti-détournement",
            "Si la pub ou un clic ouvre le Play Store ou une autre appli, revient à la pub (et mémorise les fausses croix).",
            default = true,
        ),
        AgentSetting(
            UNLABELED_BUTTONS, NO_REAL_CLOSE, "Boutons sans libellé",
            "Après 8 s sans vraie croix, tente les petites icônes en haut de l'écran.",
            default = true,
        ),
        AgentSetting(
            WAKE_PLAYABLES, NO_REAL_CLOSE, "Réveiller les mini-jeux",
            "Sans bouton après 6 s, un seul appui sur la pub pour faire apparaître « Next ».",
            default = true,
        ),
        AgentSetting(
            STORE_EXIT_BUTTONS, NO_REAL_CLOSE, "Sortir par le bouton boutique",
            "Seule sortie « Google Play » : AGAS l'utilise après 10 s puis referme la boutique (compte comme un clic sur la pub).",
            default = true,
        ),
        AgentSetting(
            BACK_FALLBACK, NO_REAL_CLOSE, "Touche Retour en dernier recours",
            "Après 45 s sans bouton trouvé. Peut faire perdre une récompense.",
            default = false,
        ),
        AgentSetting(
            SAVE_DUMPS, DIAGNOSTIC, "Enregistrer les pubs non résolues",
            "Sauvegarde leur structure pour améliorer la détection.",
            default = true,
        ),
    )

    /** Valeurs lues au début de chaque analyse. */
    class Values(host: AgentHost) {
        val dryRun = host.dryRun
        val hijackGuard = host.setting(HIJACK_GUARD)
        val unlabeledButtons = host.setting(UNLABELED_BUTTONS)
        val wakePlayables = host.setting(WAKE_PLAYABLES)
        val storeExitButtons = host.setting(STORE_EXIT_BUTTONS)
        val backFallback = host.setting(BACK_FALLBACK)
        val saveDumps = host.setting(SAVE_DUMPS)
    }
}
