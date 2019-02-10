# Essential features

## Server

* Multithreading for connection concurrency
  * Thread pools & tasks to execute.
  * Each client connection create tasks and submits it to the tasks queue.
  * What state of the application has to be thread-safe ?
  * What data has race conditions ?
  * `CopyOnWriteArrayList` is said to be the best solution for observers lists.
* `ScheduledThreadPoolExecutor`: for tasks needing to be run periodically.
* `CachedThreadPoolExecutor`: for a light server, needs little configuration and
  takes generally the correct decisions.
* Write only `Runnable` and `Callable` objects as **tasks** to execute, and give
  them to the PoolExecutors.
* Client identification => UUID associated to an account.
* State of the channel as a history.
* Channel: generic conversation between >= 2 people.
* Direct Messaging = Private channel of == 2 people.
* Group channel can be **private** or **public**.
* **Private channels** are access-controlled channels.
  * Access control is done by the **admin**
  * Admin cannot leave (?) this channel as he has to manage it.
* Distinction group messaging / private channel:
  * Group messaging : direct messaging ensures everyone receive the message
  * Private channel : only the subscribed users receive the messages
* Admin can promote non-admins to admin position.
* How do we manage demotion ?
* Who is admin when admin leaves ?
* What is leaving a room ?
* Status:
  * Available (green)
  * Busy (red)
  * Away (yellow)
  * Custom : inheriting main characteristics of three main status and composing around them with a custom label, fine-tuning notifications rules, etc.

## Client

* Search bar (contacts **and** channels at the same time)
* List of channels user is part of.
* List of DM conversations that are currently opened.
* Elements with unread messages is in **bold**.
* Unread messages : stored client-side or server-side ? Timestamp for last date of last read message or UUID of last read message ?
* Main center panel : active conversation.
* List of users in the active conversation.
* Searching list of available channels and contacts is invoqued only when needed and not the default interface. And it is the **same** interface.
* List of connected users in channel (status)
* Conditionnal display given user rights.
* Usage statistics display (only a line graph and best contributor maybe ?)
* Hover interface elements for feedback : plus button to add channel/dms, expand list of users, changing status, etc.
* What is stored client-side ?
* Update main interface coroutine : asynchronous or blocking ?

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
