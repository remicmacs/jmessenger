# Features essentielles

## Serveur

* Multithreading pour la gestion concurrente des clients
  * Pools de thread avec un maximum de clients par thread
  * Gestion du state thread-safe
* Identification des clients / alias
* Historique
* Rooms

## Client
* Plusieurs rooms ouvertes en simultané
* Liste de contacts
* Liste de rooms disponibles
* Recherche de contact et de rooms

## Concepts saillant
* Conversation
* Connexion
* Historique
* Protocole de communication
  * Plein texte plutôt que binaire (sinon, come on le debugging
  * Why not implémenter XMPP

## Overkill
* Transfert de fichiers
* Call visio
* Chiffrement