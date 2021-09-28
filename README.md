# Microblog

Simple twitter-like API.

## Features
* follow/unfollow other users
* post posts (the equivalent of tweets)
* post quotes (the equivalent of quote tweets)
* post response posts (the equivalent of twitter replies)
* like/unlike other users' posts
* tag posts
* mention other users in posts
* retrieve most popular tags in the last hour/day/week
* user's feed based on popularity/recency of posts
* notifications (when being quoted, responded to, or mentioned in another post)
* report posts
* search for users


## API

| Endpoint                         | Method | Request Params                                                               | Request body                                 | Description                                                                                                                                                                  |
|----------------------------------|--------|------------------------------------------------------------------------------|----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /api/users                       | GET    | 'username' or 'search' (either one is required, they are mutually exclusive) | -                                            | Returns a single user's info if the user having a certain 'username' exists.  If 'search' is provided, then it returns a list of users whose names contain a certain phrase. |
| /api/users/me                    | GET    | -                                                                            | -                                            | Returns the info about the user who is currently logged in.                                                                                                                  |
| /api/users/me                    | PUT    | -                                                                            | 'displayedUsername', 'description', 'aviURL' | Updates the info of the user who is currently logged in.                                                                                                                     |
| /api/users/register              | POST   | -                                                                            | 'username', 'email', 'password', 'password2' | Creates a new user using the info from the request body.                                                                                                                     |
| /api/users/{uuid}                | GET    | -                                                                            | -                                            | Returns the info about the user with specified uuid.                                                                                                                         |
| /api/users/{uuid}/profile        | GET    | -                                                                            | -                                            | Returns the followers/follows counters of the user with specified uuid.                                                                                                      |
| /api/users/{uuid}/knownFollowers | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of users who follow the user with specified uuid, and are also being followed by the user who is currently logged in.                                       |
| /api/users/{uuid}/follow         | GET    | -                                                                            | -                                            | Returns the info whether currently logged in user follows the user with specified uuid.                                                                                      |
| /api/users/{uuid}/follow         | POST   | -                                                                            | -                                            | Makes the currently logged in user follow the user with specified uuid.                                                                                                      |
| /api/users/{uuid}/follow         | DELETE | -                                                                            | -                                            | Makes the currently logged in user unfollow the user with specified uuid.                                                                                                    |
| /api/users/{uuid}/followers      | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of users who are following the user with specified uuid.                                                                                                    |
| /api/users/{uuid}/follows        | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of users who are being followed by the user with specified uuid.                                                                                            |
| /api/users/{uuid}/recentPosts    | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of the most recent posts of the user with specified uuid.                                                                                                   |
| /api/tags                        | GET    | 'name'                                                                       | -                                            | Returns the tag with specified name.                                                                                                                                         |
| /api/tags/popular                | GET    | 'since' and/or 'limit'                                                       | -                                            | Returns a list of the most popular tags.                                                                                                                                   |
| /api/tags/{uuid}/recentPosts     | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of the most recent posts tagged with the tag with specified uuid.                                                                                           |
| /api/posts/{uuid}                | GET    | -                                                                            | -                                            | Returns the post with specified uuid.                                                                                                                                        |
| /api/posts/{uuid}/info           | GET    | -                                                                            | -                                            | Returns the responses/likes/quotes counters of the post with specified uuid.                                                                                                 |
| /api/posts/{uuid}/responses      | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of responses to the post with specified uuid.                                                                                                               |
| /api/posts/{uuid}/quotes         | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of quotes of the post with specified uuid.                                                                                                                  |
| /api/posts/{uuid}/like           | GET    | -                                                                            | -                                            | Returns the info whether the user who is currently logged in likes the post with specified uuid.                                                                             |
| /api/posts/{uuid}/like           | POST   | -                                                                            | -                                            | Makes the currently logged in user like the post with specified uuid.                                                                                                        |
| /api/posts/{uuid}/like           | DELETE | -                                                                            | -                                            | Makes the currently logged in user unlike the post with specified uuid.                                                                                                      |
| /api/posts                       | POST   | -                                                                            | 'content'                                    | Creates a new post (the author is the user who makes the request) that contains the text provided in the 'content' field of the request body.                                |
| /api/posts/{uuid}/responses      | POST   | -                                                                            | 'content'                                    | Creates a response to the post with specified uuid (the author of the response is the user who makes the request).                                                           |
| /api/posts/{uuid}/quotes         | POST   | -                                                                            | 'content'                                    | Creates a quote of the post with specified uuid (the author of the quote is the user who makes the request).                                                                 |
| /api/posts/{uuid}                | DELETE | -                                                                            | -                                            | Deletes the post with specified uuid (if the user who makes the request is the author of that post).                                                                         |
| /api/reports                     | GET    | 'skip' and/or 'limit' and/or 'checked'                                       | -                                            | Returns a list of reported posts.                                                                                                                                          |
| /api/reports                     | POST   | -                                                                            | 'reportedPostUuid', 'reason', 'description'  | Reports the post with uuid given in 'reportedPostUuid'.                                                                                                                      |
| /api/reports/{uuid}              | POST   | 'accept'                                                                     | -                                            | Marks the report with specified uuid as 'checked'. Whether the reported post gets removed depends on the value of the 'accept' parameter.                                    |
| /api/notifications               | GET    | 'skip' and/or 'limit'                                                        | -                                            | Returns a list of notifications of the currently logged in user.                                                                                                           |
| /api/notifications/unreadCounter | GET    | -                                                                            | -                                            | Returns the number of unread notifications of the currently logged in user.                                                                                                  |
| /api/notifications/readAll       | POST   | -                                                                            | -                                            | Marks all notifications of the currently logged in user as 'read'.                                                                                                           |
| /api/notifications/{uuid}/read   | POST   | -                                                                            | -                                            | Mark the notification with specified uuid as 'read'.                                                                                                                         |
| /api/feed                        | GET    | 'skip' and/or 'limit'                                            | -                                                        | Get the feed of the currently logged in user (only the most recent posts).                                                                                                   |
| /api/feed/popular                | GET    | 'skip' and/or 'limit'                                            | -                                                        | Get the feed of an anonymous user or the currently logged in user (only the most popular posts).                                                                             |

### GET /api/users

Requires auth: no

###### Request params:
* username - provides exact username of a user whose data should be returned
* search - provides a phrase that should be present in usernames of users whose data should be returned

Either one of these parameters is required, but both cannot be provided at the same time.
###### Request body: -

###### Example request:
**GET /api/users?search=test**

Response body:
```JSON
[
   {
      "uuid":"7ef1baf1-86fe-4901-8a3f-3edd138a8776",
      "username":"testuser",
      "displayedUsername":"testuser",
      "description":"",
      "aviURL":""
   },
   {
      "uuid":"b829b4bd-0ee2-443e-aa41-3791913e0f1a",
      "username":"testuser123",
      "displayedUsername":"testuser123",
      "description":"",
      "aviURL":""
   }
]
```

*** 
### GET /api/users/me

Requires auth: yes

###### Request params: -
###### Request body: -

###### Example request:
**GET /api/users/me**

Response body:
```JSON
{
   "uuid":"7ef1baf1-86fe-4901-8a3f-3edd138a8776",
   "username":"testuser",
   "displayedUsername":"testuser",
   "description":"",
   "aviURL":""
}
```
*** 
### PUT /api/users/me

Requires auth: yes

###### Request params: -
###### Request body: 

* displayedUsername - minimum length 1, max length 70
* description - minimum length 1, max length 200
* aviURL - optional

```JSON
{
  "displayedUsername": "",
  "aviURL": "",
  "description": ""
}
```
###### Example request:-
*** 
### POST /api/users/register

Requires auth: no

###### Request params: -
###### Request body:

* email needs to be valid
* username must have between 1-30 characters, and contain characters a-z/A-Z/0-9
* password must have between 8-64 characters, and contain characters a-z/A-Z/0-9 + [special characters](https://owasp.org/www-community/password-special-characters)
* password2 must be the same as password

```JSON
{
    "email": "",
    "username": "",
    "password": "",
    "password2": ""
}
```

###### Example request:-
*** 
### GET /api/users/{uuid}

Requires auth: no

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/users/5f8dc959-611e-4910-a456-68a5e467b859**

Response body:
```JSON
{
    "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
    "username":"mateusz",
    "displayedUsername":"mateusz",
    "description":"",
    "aviURL":""
}
```
*** 
### GET /api/users/{uuid}/profile

Requires auth: no

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/users/5f8dc959-611e-4910-a456-68a5e467b859/profile**

Response body:
```JSON
{
    "follows":2,
    "followers":0
}
```
*** 
### GET /api/users/{uuid}/knownFollowers

Requires auth: yes

###### Request params:
* skip - how many results should be skipped (defaults to 0 if not provided)
* limit - how many results at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/knownFollowers**

Response body:
```JSON
[
  {
      "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
      "username":"mateusz",
      "displayedUsername":"mateusz",
      "description":"",
      "aviURL":""
  }
]
```
*** 
### GET /api/users/{uuid}/follow

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/follow**

Response body:
```JSON
{
    "follows":false
}
```
*** 
### POST /api/users/{uuid}/follow

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**POST /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/follow**

Response body:
```JSON
{
    "follows":true
}
```
*** 
### DELETE /api/users/{uuid}/follow

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**DELETE /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/follow**

Response body:
```JSON
{
    "follows":false
}
```
*** 
### GET /api/users/{uuid}/followers

Requires auth: no

###### Request params:
* skip - how many followers should be skipped (defaults to 0 if not provided)
* limit - how many followers at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/followers?skip=0**

Response body:
```JSON
[
  {
      "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
      "username":"mateusz",
      "displayedUsername":"mateusz",
      "description":"",
      "aviURL":""
  }
]
```
*** 
### GET /api/users/{uuid}/follows

Requires auth: no

###### Request params:
* skip - how many follows should be skipped (defaults to 0 if not provided)
* limit - how many follows at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/users/4129a231-5307-47de-bc00-7031d0ecc31c/follows?skip=0**

Response body:
```JSON
[
  {
      "uuid":"f3ff9de6-0107-484e-882f-ddfe6ebfd2d9",
      "username":"test",
      "displayedUsername":"test",
      "description":"",
      "aviURL":""
  }
]
```
*** 
### GET /api/users/{uuid}/recentPosts

Requires auth: no

###### Request params: 
* skip - how many posts to skip (defaults to 0 if not provided)
* limit - how many posts at most should be returned (defaults to 10 if not provided)

###### Request body: -
###### Example request:
**GET /api/users/58f89413-a507-44eb-884b-20719dcbd74b/recentPosts?skip=0**

Response body:
```JSON
[
  {
    "uuid":"2c0ac694-8c86-449f-a9ed-7b053928880d",
    "content":"Another test post",
    "date":"2021-09-26T19:48:04.158+00:00",
    "author":{
      "uuid":"58f89413-a507-44eb-884b-20719dcbd74b",
      "username":"testmail",
      "displayedUsername":"testmail",
      "description":"",
      "aviURL":""
    },
    "quotes":null,
    "respondsTo":null,
    "respondsToUsername":null
  },
  {
    "uuid":"9094d54f-3d86-44be-af26-88a51b56b87f",
    "content":"Test post ",
    "date":"2021-09-26T19:47:57.161+00:00",
    "author":{
      "uuid":"58f89413-a507-44eb-884b-20719dcbd74b",
      "username":"testmail",
      "displayedUsername":"testmail",
      "description":"",
      "aviURL":""
    },
    "quotes":null,
    "respondsTo":null,
    "respondsToUsername":null
  }
]
```
*** 
### GET /api/tags

Requires auth: no

###### Request params:
* name - name of the tag (required)

###### Request body: -
###### Example request:
**GET /api/tags?name=test**

Response body:
```JSON
{
    "uuid":"bdc911f6-6af8-4b8b-9a62-663e78631439",
    "name":"test"
}
```
*** 
### GET /api/tags/popular

Requires auth: no

###### Request params:
* since - how much time has to be taken into account when searching for the most popular tags (accepts 'hour'/'day'/'week' and defaults to 'hour' if not provided)
* limit - how many tags at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/tags/popular?since=hour**

Response body:
```JSON
[
  {
      "uuid":"bdc911f6-6af8-4b8b-9a62-663e78631439",
      "name":"test"
  }
]
```
*** 
### GET /api/tags/{uuid}/recentPosts

Requires auth: no

###### Request params:
* skip - how many posts should be skipped (defaults to 0 if not provided)
* limit - how many posts at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/tags/bdc911f6-6af8-4b8b-9a62-663e78631439/recentPosts**

Response body:
```JSON
[
   {
      "uuid":"d784bf6a-0c28-42aa-9eb8-510a1faf726d",
      "content":"Test #test",
      "date":"2021-09-26T19:50:47.946+00:00",
      "author":{
         "uuid":"58f89413-a507-44eb-884b-20719dcbd74b",
         "username":"testmail",
         "displayedUsername":"testmail",
         "description":"",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":null
   },
   {
      "uuid":"736199d5-f275-4155-9ca6-7b80e711487d",
      "content":"Tagged #test. ",
      "date":"2021-09-08T14:29:59.993+00:00",
      "author":{
         "uuid":"44bed729-76d2-46db-ba67-e4062222c287",
         "username":"newuser",
         "displayedUsername":"New User",
         "description":"This is my description",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":null
   },
   {
      "uuid":"0c66c0b9-e7f6-4c28-8927-66e5e176b237",
      "content":"#test content",
      "date":"2021-08-24T14:02:32.094+00:00",
      "author":{
         "uuid":"7ef1baf1-86fe-4901-8a3f-3edd138a8776",
         "username":"testuser",
         "displayedUsername":"Test user",
         "description":"My description",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":null
   }
]
```
*** 
### GET /api/posts/{uuid}

Requires auth: no

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/posts/d784bf6a-0c28-42aa-9eb8-510a1faf726d**

Response body:
```JSON
{
   "uuid":"d784bf6a-0c28-42aa-9eb8-510a1faf726d",
   "content":"Test #test",
   "date":"2021-09-26T19:50:47.946+00:00",
   "author":{
      "uuid":"58f89413-a507-44eb-884b-20719dcbd74b",
      "username":"testmail",
      "displayedUsername":"testmail",
      "description":"",
      "aviURL":""
   },
   "quotes":null,
   "respondsTo":null,
   "respondsToUsername":null
}
```
*** 
### GET /api/posts/{uuid}/info

Requires auth: no

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/posts/d784bf6a-0c28-42aa-9eb8-510a1faf726d/info**

Response body:
```JSON
{
    "responses":0,
    "likes":0,
    "quotes":0
}
```
*** 
### GET /api/posts/{uuid}/responses

Requires auth: no

###### Request params: 
* skip - how many responses should be skipped (defaults to 0 if not provided)
* limit - how many responses at most should be returned (defaults to 5 if not provided)
###### Request body: -
###### Example request:
**GET /api/posts/0c66c0b9-e7f6-4c28-8927-66e5e176b237/responses**

Response body:
```JSON
[
   {
      "uuid":"c482cf30-d8f1-473d-a34f-dfb189b3f823",
      "content":"Lorem Ipsum dolor sit amet",
      "date":"2021-09-08T14:30:30.572+00:00",
      "author":{
         "uuid":"44bed729-76d2-46db-ba67-e4062222c287",
         "username":"newuser",
         "displayedUsername":"New User",
         "description":"This is my description",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":"0c66c0b9-e7f6-4c28-8927-66e5e176b237",
      "respondsToUsername":"testuser"
   }
]
```
*** 
### GET /api/posts/{uuid}/quotes

Requires auth: no

###### Request params:
* skip - how many quotes should be skipped (defaults to 0 if not provided)
* limit - how many quotes at most should be returned (defaults to 5 if not provided)
###### Request body: -
###### Example request:
**GET /api/posts/ffd8cedf-442d-43b1-a553-032983a41fbc/quotes**

Response body:
```JSON
[
  {
    "uuid":"64e8fb7b-e0bc-4d41-847e-f35b5d423a99",
    "content":"Test quote",
    "date":"2021-08-31T13:07:39.605+00:00",
    "author":{
      "uuid":"f3ff9de6-0107-484e-882f-ddfe6ebfd2d9",
      "username":"test",
      "displayedUsername":"test",
      "description":"",
      "aviURL":""
    },
    "quotes":"ffd8cedf-442d-43b1-a553-032983a41fbc",
    "respondsTo":null,
    "respondsToUsername":null
  }
]
```
*** 
### GET /api/posts/{uuid}/like

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/posts/4cd5bc2d-10e2-43ab-a3ca-655aee2af3e9/like**

Response body:
```JSON
{
    "likes":false
}
```
*** 
### POST /api/posts/{uuid}/like

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**POST /api/posts/dbe0328e-e223-4c81-9f09-0ffd27daec86/like**

Response body:
```JSON
{
    "likes":true
}
```
*** 
### DELETE /api/posts/{uuid}/like

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**DELETE /api/posts/dbe0328e-e223-4c81-9f09-0ffd27daec86/like**

Response body:
```JSON
{
    "likes":false
}
```
*** 
### POST /api/posts

Requires auth: yes

###### Request params: -
###### Request body: 
* content - between 1-300 characters

Words that start with '#' and are 2-20 characters (a-z/A-Z/0-9) long are recognized as tags.
Words that start with '@' and are valid usernames are recognized as attempts of mentioning another user.

```JSON
{
    "content": "Test #post"
}
```
###### Example request:
**POST /api/posts**

Response body:
```JSON
{
    "uuid":"19b1691f-dfae-4b22-9876-9e6a33e7c9d8"
}
```
*** 
### POST /api/posts/{uuid}/responses

Requires auth: yes

###### Request params: -
###### Request body:
* content - between 1-300 characters

Words that start with '#' and are 2-20 characters (a-z/A-Z/0-9) long are recognized as tags.
Words that start with '@' and are valid usernames are recognized as attempts of mentioning another user.

```JSON
{
    "content": "Test response"
}
```
###### Example request:
**POST /api/posts/19b1691f-dfae-4b22-9876-9e6a33e7c9d8/responses**

Response body:
```JSON
{
    "uuid":"08bd1387-3aeb-403c-9f6a-89ede18dce11"
}
```
*** 
### POST /api/posts/{uuid}/quotes

Requires auth: yes

###### Request params: -
###### Request body:
* content - between 1-300 characters

Words that start with '#' and are 2-20 characters (a-z/A-Z/0-9) long are recognized as tags.
Words that start with '@' and are valid usernames are recognized as attempts of mentioning another user.

```JSON
{
    "content": "Test quote"
}
```
###### Example request:
**POST /api/posts/19b1691f-dfae-4b22-9876-9e6a33e7c9d8/quotes**

Response body:
```JSON
{
    "uuid":"7417443b-f522-4717-b5b7-4100b578151d"
}
```
*** 
### DELETE /api/posts/{uuid}

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**DELETE /api/posts/7417443b-f522-4717-b5b7-4100b578151d**

Response body:
```JSON
{
    "deleted": true
}
```
*** 
### GET /api/reports

Requires auth: yes (user must have admin privileges)

###### Request params: 
* skip - how many reports to skip (defaults to 0 if not provided)
* limit - how many reports at most to return (defaults to 20 if not provided)
* checked - whether the status of returned reports should be 'checked' or 'not checked' by the moderator (defaults to false) 

###### Request body: -
###### Example request:
**GET /api/reports?checked=false**

Response body:
```JSON
[
   {
      "uuid":"46c41156-3260-40c0-b11a-34a786de03cb",
      "reportedPostUuid":"dbe0328e-e223-4c81-9f09-0ffd27daec86",
      "reportAuthorUsername":"mateusz",
      "postAuthorUsername":"testuser",
      "postContent":"Test test",
      "postDeleted":false,
      "reason":"SPAM",
      "checked":false,
      "description":"This post contains spam"
   }
]
```
*** 
### POST /api/reports

Requires auth: yes

###### Request params: -
###### Request body: 
* reportedPostUuid - uuid of the post that's being reported (required)
* reason - valid values are: 'spam', 'abusive' or 'against_tos' (required)
* description - optional description containing the detailed reasoning behind the report

```JSON
{
    "reportedPostUuid": "",
    "reason": "",
    "description": ""
}
```

###### Example request:
**POST /api/reports**

Response body:
```JSON
{
    "created": true
}
```
*** 
### POST /api/reports/{uuid}

Requires auth: yes (user must have admin privileges)

###### Request params: 
* accept - if set to 'true' reported post will be deleted, otherwise it will be left unchanged

###### Request body: -
###### Example request:
**POST /api/reports/0657e584-9801-42eb-9e02-a628a57c5be9?accept=false**

Response body:
```JSON
{
    "checked": true
}
```
*** 
### GET /api/notifications

Requires auth: yes

###### Request params: 
* skip - how many notifications should be skipped (defaults to 0 if not provided)
* limit - how many notifications at most should be returned (defaults to 5 if not provided)

###### Request body: -
###### Example request:
**GET /api/notifications**

Response body:
```JSON
[
   {
      "uuid":"5d6dbb31-e0b1-4687-9f9c-e3d84bee5f37",
      "notifiedBy":"mateusz",
      "read":true,
      "type":"response",
      "notificationPost":"4cd5bc2d-10e2-43ab-a3ca-655aee2af3e9"
   },
   {
      "uuid":"5ed16cd4-340e-4d55-888c-9ed9fe2c8cff",
      "notifiedBy":"mateusz",
      "read":false,
      "type":"response",
      "notificationPost":"280d7fec-4b49-4354-b6a3-b44da643f4ce"
   },
   {
      "uuid":"e1ff084f-84af-4fcc-a304-7b5c6524ed65",
      "notifiedBy":"mateusz",
      "read":false,
      "type":"response",
      "notificationPost":"11c383f4-f41a-4422-83a5-00f9c5925b7d"
   },
   {
      "uuid":"23fd073c-3b12-492a-9c97-2e5e68f723c5",
      "notifiedBy":"mateusz",
      "read":false,
      "type":"quote",
      "notificationPost":"a0f2e39a-8ab3-43da-9918-99a78becd61d"
   },
   {
      "uuid":"a58475bb-b8fd-4076-a1be-0613557829f0",
      "notifiedBy":"mateusz",
      "read":true,
      "type":"response",
      "notificationPost":"36686134-fca5-480b-8a4f-5ecab4c75167"
   }
]
```
*** 
### GET /api/notifications/unreadCounter

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**GET /api/notifications/unreadCounter**

Response body:
```JSON
{
    "unreadCounter":3
}
```
*** 
### POST /api/notifications/readAll

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**POST /api/notifications/readAll**

Response body:
```JSON
{
    "markedAsRead":3
}
```
*** 
### POST /api/notifications/{uuid}/read

Requires auth: yes

###### Request params: -
###### Request body: -
###### Example request:
**POST /api/notifications/5ed16cd4-340e-4d55-888c-9ed9fe2c8cff/read**

Response body:
```JSON
{
    "read":true
}
```
*** 
### GET /api/feed

Requires auth: yes

###### Request params: 
* skip - how many posts should be skipped (defaults to 0 if not provided)
* limit - how many posts at most should be returned (defaults to 20 if not provided)

###### Request body: -
###### Example request:
**GET /api/feed**

Response body:
```JSON
[
   {
      "uuid":"08bd1387-3aeb-403c-9f6a-89ede18dce11",
      "content":"Test @mateusz :)",
      "date":"2021-09-27T13:23:49.569+00:00",
      "author":{
         "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
         "username":"mateusz",
         "displayedUsername":"mateusz",
         "description":"",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":"19b1691f-dfae-4b22-9876-9e6a33e7c9d8",
      "respondsToUsername":"mateusz"
   }
]
```
*** 
### GET /api/feed/popular

Requires auth:
* if provided, the response contains the most popular posts selected for the currently logged in user
* if not provided, the response contains the most popular posts globally in the previous 24 hours

###### Request params:
* skip - how many posts should be skipped (defaults to 0 if not provided)
* limit - how many posts at most should be returned (defaults to 20 if not provided)

###### Request body: -
###### Example request:
**GET /api/feed/popular**

Response body:
```JSON
[
   {
      "uuid":"08bd1387-3aeb-403c-9f6a-89ede18dce11",
      "content":"Test @mateusz :)",
      "date":"2021-09-27T13:23:49.569+00:00",
      "author":{
         "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
         "username":"mateusz",
         "displayedUsername":"mateusz",
         "description":"",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":"19b1691f-dfae-4b22-9876-9e6a33e7c9d8",
      "respondsToUsername":"mateusz"
   },
   {
      "uuid":"19b1691f-dfae-4b22-9876-9e6a33e7c9d8",
      "content":"Test #post",
      "date":"2021-09-27T13:15:23.930+00:00",
      "author":{
         "uuid":"5f8dc959-611e-4910-a456-68a5e467b859",
         "username":"mateusz",
         "displayedUsername":"mateusz",
         "description":"",
         "aviURL":""
      },
      "quotes":null,
      "respondsTo":null,
      "respondsToUsername":null
   }
]
```