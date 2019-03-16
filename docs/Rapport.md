# Rapport de Projet Java

16 Mars 2019 - ISEN AP4

* Rémi Bourgeon
* Rodolphe Houdas

## Introduction

Dans le cadre du cours de Java, nous devons réaliser une messagerie instantanée. À la manière d'un logiciel comme Slack ou Riot, celui-ci doit permettre de gérer des conversations directes ou des salons.

Le cahier des charges définit plusieurs fonctionnalités. Dans la suite de ce rapport, nous essaierons d'expliquer quelles solutions nous avons adopté.

## Architecture

Le logiciel est articulée autour d'une communication entre un serveur et des clients.

Le logiciel a été développé avec la version 8 du JDK.

### Protocole de communication

Les messages sont échangés à travers les WebSocket qui permettent, à travers des sockets TCP, d'assurer l'intégrité des messages ainsi que la phase de connexion (*handshake*).

Tous les messages sont sérialisés à l'aide de GSON. Tous les salons et utilisateurs possèdent un UUID permettant de les identifier de manière unique.

Un salon administrateur est automatiquement mis en place par le serveur à l'UUID `0x0`. Celui ci permet d'envoyer des requêtes au serveur pour la création d'un salon, l'ajout d'un utilisateur à un salon ou encore pour recevoir l'historique d'un salon.

## Fonctionnalités

### Gestion des salons

Dans notre projet, tout échange entre utilisateurs se fait à travers un salon. On définit donc 3 types de salons :

* Conversation : aussi appelée messagerie directe, elle comprend un ou plusieurs utilisateurs. La liste des participants est fixée lors de la création de la conversation
* Salon privé : injoignable sauf si un utilisateur a été ajouté à la liste des participants autorisés
* Salon public : un salon joignable par n'importe qui n'ayant pas été banni par l'administrateur

Les salons privés et publics possèdent un ou plusieurs administrateurs qui peuvent virer, bannir ou promouvoir un participant.

Les modèles permettant la gestion des salons sont définis dans les classes :

* `AbstractChannel`
* `AbstractRoom`
* `PrivateRoom`
* `PublicRoom`
* `DirectMessageConversation`

Ainsi que dans l'interface `Channel`.

Lorsqu'un message est envoyé, celui-ci contient l'UUID du salon cible, ce qui permet au serveur de sélectionner les participants au salon cible et de leur envoyer sélectivement le message.

Les utilisateurs peuvent quitter un salon, ce qui les désinscrit de la liste des participants du salon.

### Gestion de droits

Dans un salon, les participants peuvent être simple utilisateur ou administrateur. L'administrateur possède les droits pour virer, bannir, promouvoir ou inviter un participant.

### Interface graphique

L'interface graphique a été développée en JavaFX.

### Export de l'historique en XML
