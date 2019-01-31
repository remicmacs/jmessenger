# Essential features

## Server

* Multithreading for connection concurrency
  * Thread pools & tasks to execute.
  * Each client connection create tasks and submits it to the tasks queue.
  * What state of the application has to be thread-safe ?
  * What data has race conditions
* Client identification => UUID associated to an account.
* State of the channel as a history.
* Channel: generic conversation between >= 2 people.
* Direct Messaging = Private channel of == 2 people.
* Group channel can be **private** or **public**.
* **Private channels** are access-controlled channels.
  * Access control is done by the **admin**
  * Admin cannot leave (?) this channel as he has to manage it.
* Distinction group messaging / private channel (please no).
* Admin can promote non-admins to admin position.
* How do we manage demotion ?
* Who is admin when admin leaves ?
* What is leaving a room ?

## Client
* List of channel user is part of.
* List of DM conversations that are currently opened.
* Searching list of available channels and contacts is invoqued only when needed and not the default interface. And it is the **same** interface.
* Recherche de contact et de rooms
* What is stored client-side ?
* List of connected users in channel (status)
* Usage statistics display (only a line graph and best contributor maybe ?)

## Concepts
* Conversation
* Channel
* Connections
* History
* Private / Public
* Admin
* Status
* Communication protocol
  * Messaging protocol
  * Must be full text for easier debugging

