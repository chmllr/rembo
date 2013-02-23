# Rembo: Recursive Message Board

Rembo is a core engine providing a simple recursive message board.
It is completely decoupled from the presentation layer (apps, web, etc.) and provides solely an API (see below).

## The Idea

The idea is very simple: every message represents a post and a comment simultaneously.

- High level messages are comments of special root posts (many roots can exist).
- The comments of every message are post themselves (and can have comments).

Hence, every comment to a comment of some post is a comment of a post too.
This idea aims to be orthogonal to the hierarchical representation of comments.

## Content management

Messages can be:

- created
- edited (this will be noticed in the message state forever)
- upvoted

## User management

User management is handled by the Rembo core and supports:

- authorization
- authentication
- user info changes

User can post pseudo-anonymously (with name hidden from other users).

User's karma is computed from the upvotes of all his posts.

## Persistence

The persistence layer is build into Rembo and uses redis store.

### Schema

| Key             | Hierarchy Key | Type   | Meaning                                 |
|-----------------|---------------|--------|-----------------------------------------|
| last_user_id    |               | int    | last _used_ user ID                     |
| <id>:name       | users         | string | user name                               |
| <id>:password   | users         | string | user password                           |
| <id>:email      | users         | string | user's email                            |
| <id>:about      | users         | string | about the user                          |
| last_message_id |               | int    | last _used_ message ID                  |
| <id>:message    | messages      | string | message                                 |
| <id>:created    | messages      | int    | message's creation date in POSIX format |
| <id>:updated    | messages      | int    | message's update date in POSIX format   |
| <id>:author     | messages      | int    | author's ID                             |
| <id>:parent     | messages      | int    | parent message ID                       |
| <id>:visible    | messages      | bool   | visibility status of the message        |
| <id>:anonymous  | messages      | bool   | anonymity status of the message         |
| <id>:children   |               | set    | IDs of children messages                |
| <id>:upvotes    |               | set    | user ID's who upvoted                   |
| <user_name>     | name2id       | int    | maps user names to IDs                  |

## Rembo API

### User Management

#### User Creation

- Function: `user-create`
- Input: `{'name': <name>, 'password': <password>, 'about': <about>, 'email': <email>}`

#### User Authentication

- Function: `user-authenticate`
- Input: `{'name': <name>, 'password': <password>}`
- Output: `{'user-id': <id>, 'auth-token': <token>}`

#### User Update

- Function: `user-update`
- Input: `{'user-id': <user-id>, 'auth-token': <token>, <field>: <content>}`

The `<field>` can be `name`, `email`, `about`, `password`.
Several fields can be specified simultaneosly.

#### User Retrieval

- Function: `user-retrieve`
- Input: `{'user-id': <user-id>}`
- Output: `{'name': <name>, 'about': <about>, 'email': <email>}`

#### User Meta-Information Retrieval

- Function: `user-meta-retrieve`
- Input: `{'user-id': <user-id>}`
- Output: `{'posts': [node IDs], 'upvotes': [node IDs]}`

The meta information represents an information which is expensive to retrieve and will be derived from other informations.

### Content Management

#### Message Creation

- Function: `message-create`
- Input: `{'user-id': <user-id>, 'auth-token': <token>, 'message': <message>, 'parent': <node-id>, 'anonymously': <bool>}`

#### Message Update

- Function: `message-update`
- Input: `{'user-id': <user-id>, 'auth-token': <token>, 'node-id': <node-id>, 'message': <message>}`

#### Message Upvote

- Function: `message-upvote`
- Input: `{'user-id': <user-id>, 'auth-token': <token>, 'node-id': <node-id>}`

#### Message Retrieval

- Function: `message-retrieve`
- Input: `{'node-id': <node-id>}`
- Output `{'message': <message>, 'parent': <node ID>, 'children': [node IDs], 'upvotes': [user IDs], 'created': <date>, 'updated': <date>, 'author': <user-id>}`

Note that the `author` might be missing in case when the post was made anonymously.
