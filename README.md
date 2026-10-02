# AGAS Agent

La logique de passage des pubs d'**AGAS — Android Games Ads Skipper** : comment reconnaître une
pub, trouver son bouton de fermeture et déjouer ses pièges.

L'Agent n'est **pas une appli à installer**. C'est un APK sans écran que
[AGAS Manager](https://github.com/NzoSifou/AGAS-Manager) télécharge depuis les releases de ce
dépôt, vérifie (signature, version du contrat) et charge à chaud dans son service
d'accessibilité. Publier une nouvelle version de l'Agent met donc à jour la façon de passer les
pubs pour tout le monde, sans réinstaller l'appli.

AGAS **ne bloque aucune publicité** : la pub s'affiche normalement et le développeur du jeu est
rémunéré. AGAS appuie seulement sur le bouton de fermeture à ta place.

## Fonctionnement

L'Agent lit la structure de l'écran (l'« arbre d'accessibilité »), pas les pixels :

1. **Détection de la pub** : ouverture d'un écran appartenant à une régie publicitaire connue
   (AdMob, AppLovin, Unity, ironSource, Vungle, Mintegral, Moloco, Fyber, BidMachine, Pangle…).
2. **Recherche du bouton de fermeture** : textes et descriptions (« Close », « Skip », « Fermer »,
   « Next »…), identifiants (`close_button`, `skip`…), croix dessinées en SVG, petites icônes dans
   un coin. Les boutons d'installation ou de boutique sont exclus.
3. **Clic** dès que le bouton apparaît et devient cliquable (analyse toutes les 400 ms et à chaque
   changement de l'écran).
4. **Vérification** : si un clic ouvre le Play Store, AliExpress, un navigateur… (fausse croix), AGAS
   revient à la pub, évite cet endroit et mémorise le piège pour les pubs suivantes.

Cas particuliers gérés :

- **Récompenses** : attente des comptes à rebours (« Reward in 25 s »), et clic sur « Reprendre »
  si une fenêtre « vous allez perdre votre récompense » apparaît.
- **Mini-jeux (playables)** : un seul appui de « réveil » pour faire apparaître le bouton « Next »
  (jamais un second, qui ouvrirait la boutique), et, en dernier recours, utilisation du bouton
  « Google Play » / « Ouvrir la boutique » quand c'est la seule sortie, suivie de la fermeture de la
  boutique.
- **Redirections automatiques** : si la pub ouvre d'elle-même le Play Store, AGAS le referme.

## Ajouter un nouveau cas de pub

1. Récupérer la structure de la pub non résolue : AGAS Manager l'enregistre dans
   `Android/data/fr.nzosifou.agas/files/dumps/` (arbre `.txt` et capture `.png`).
2. Si une règle suffit (nouvelle régie, nouveau libellé, nouvel identifiant…), modifier
   [`ad_rules.json`](agent/src/main/resources/fr/nzosifou/agas/agent/rules/ad_rules.json) et
   augmenter son champ `version`. Sinon, modifier le code.
3. Ajouter un test, augmenter `versionCode` et `versionName` dans `agent/build.gradle.kts`, compléter
   le changelog.
4. Essayer sur un téléphone avec un Manager de debug (voir ci-dessous), puis publier.

## Développement

Ce dépôt se clone à côté d'AGAS Manager : l'Agent compile contre le contrat
(`agent-api/`) qui s'y trouve. Il n'embarque ni ce contrat ni la bibliothèque Kotlin : le Manager
les fournit à l'exécution.

```bash
git clone https://github.com/NzoSifou/AGAS-Manager.git
git clone https://github.com/NzoSifou/AGAS-Agent.git
cd AGAS-Agent
./gradlew testDebugUnitTest    # tests unitaires (règles, anti-détournement, croix SVG)
./gradlew assembleDebug        # agent/build/outputs/apk/debug/agent-debug.apk
./gradlew assembleRelease      # agent/build/outputs/apk/release/agent-release.apk (signé)
```

**Essayer une version de développement** avec un Manager de debug (signé avec la même clé de
debug) :

```bash
adb push agent/build/outputs/apk/debug/agent-debug.apk /sdcard/Android/data/fr.nzosifou.agas/files/agent-dev.apk
adb shell am broadcast -a fr.nzosifou.agas.LOAD_AGENT -p fr.nzosifou.agas
```

**Publier** : l'APK de release doit être signé avec la clé de publication d'AGAS
(`keystore.properties` à la racine, hors dépôt, comme pour le Manager) ; le Manager refuse tout
autre signataire. Créer une release GitHub `vX.Y.Z` avec l'APK en pièce jointe
(`AGAS-Agent-vX.Y.Z.apk`) : les Managers l'installent dans les 12 h.

**Contrat** : la version du contrat (`AgentApi.VERSION` dans AGAS Manager) est inscrite dans le
manifeste de l'Agent à la compilation. Un Agent compilé contre un contrat plus récent que celui
d'un Manager est refusé par celui-ci ; il ne faut donc utiliser un nouvel élément du contrat qu'une
fois le Manager correspondant publié.

Structure du code (`agent/src/main/java/fr/nzosifou/agas/agent/`) :

| Fichier / dossier | Contenu |
|---|---|
| `AdSkipperAgent` | Point d'entrée : suivi des pubs, boucle d'analyse, clics, retour au jeu |
| `AgentSettings` | Réglages déclarés au Manager (anti-détournement, mini-jeux…) |
| `detection/` | `CloseButtonFinder` (analyse de l'écran), `SvgIcons` (croix SVG), `TreeDumper` |
| `guard/` | `HijackGuard` : détection des détournements après un clic |
| `rules/` + `resources/…/ad_rules.json` | Règles de détection (régies, libellés, pièges connus…) |
| `data/` | `TrapMemory` : mémoire des fausses croix |

## Changelog

### [1.0.0] — non publiée

Première version, extraite d'AGAS 1.0.

**Détection et fermeture**

- Détection des pubs plein écran d'une quarantaine de régies publicitaires.
- Recherche du bouton de fermeture par texte, description, identifiant, croix SVG et icônes sans
  libellé ; boutons d'installation et de boutique exclus.
- Pubs en plusieurs étapes (vidéo → « Skip » → écran de fin) et popups dans la pub.
- Contenu des pubs HTML (WebView) lu en direct (cache d'accessibilité désactivé).
- Clic d'accessibilité ou appui simulé selon le cas ; jamais d'appui simulé sur un bouton pas encore
  cliquable (il traverserait jusqu'à la pub).

**Protection**

- Anti-détournement : retour à la pub si un clic ou la pub elle-même ouvre le Play Store, un
  navigateur ou une autre appli, y compris à travers des redirections en chaîne.
- Mémoire des pièges : un bouton qui a ouvert la boutique est évité pour ce type de pub.
- Protection des récompenses (comptes à rebours, fenêtre « récompense perdue »).

**Mini-jeux**

- Appui de réveil unique pour faire apparaître « Next » (pas pendant une vidéo ni un compte à
  rebours).
- Bouton de sortie « Google Play » / « Ouvrir la boutique » en dernier recours, puis fermeture de la
  popup du Play Store.
