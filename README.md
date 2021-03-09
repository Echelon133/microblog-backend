# Microblog

Simple twitter-like API.

## Features
* following other users
* posting posts (the equivalent of tweets)
* posting quotes (the equivalent of retweets)
* posting response posts (the equivalent of twitter responses)
* liking/unliking other users posts
* tagging posts with tags just by mentioning them in the post content
* retrieving most popular tags in the last hour/day/week
* getting user feed based on popularity/recency of posts 

## API

| Endpoint                                 | Method     | Request body     | Description                                      |
|------------------------                  |----------  |----------------- |-------------                                     |
| /api/users/me                            | GET        |                  | Get info about the user that is currently logged in |
| /api/users/me                            | PUT        | [User details]()     | Update displayed username, description and profile picture of currently logged user |
| /api/users/{uuid}                        | GET        |                  | Get info about the user with uuid |
| /api/users/{uuid}/profile                | GET        |                  | Get counters that show how many follows/followers the user with uuid has |
| /api/users/{uuid}/follow                 | GET        |                  | Get a response that shows whether currently logged user follows the user with uuid |
| /api/users/{uuid}/follow                 | POST       |                  | Make currently logged user follow the user with uuid |
| /api/users/{uuid}/unfollow               | POST       |                  | Make currently logged user unfollow the user with uuid |
| /api/users/{uuid}/followers?skip&limit   | GET        |                  | Get a list of users that follow the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/follows?skip&limit     | GET        |                  | Get a list of users that are followed by the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/recentPosts?skip&limit| GET        |                  | Get a list of most recent posts of user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 10 respectively |
| /api/users?username                      | GET        |                  | Get the user with exact username as given in the parameter 'username'. This parameter is required |
| /api/tags?name                           | GET        |                  | Get the tag with exact name. The parameter 'name' is required |
| /api/tags/popular?since&limit            | GET        |                  | Get a list of tags popular within a certain frame. The parameter 'since' can be set to values: *HOUR/DAY/WEEK*. If not provided, 'since' is set to *HOUR* and 'limit' is set to 5 |
| /api/tags/{uuid}/recentPosts?skip&limit | GET        |                  | Get a list of most recent posts tagged with the tag with given uuid |                
| /api/posts/{uuid}                       | GET        |                  | Get the post with uuid |
| /api/posts/{uuid}/info                  | GET        |                  | Get post's response/like/quote counters |
| /api/posts/{uuid}                       | DELETE     |                  | Mark the post with uuid as deleted |
| /api/posts/{uuid}/responses?skip&limit  | GET        |                  | Get responses to the post with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/posts/{uuid}/quotes?skip&limit   | GET        |                  | Get quotes of the post with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/posts/{uuid}/like                  | GET        |                  | Get info whether the currently logged user likes the post with uuid |
| /api/posts/{uuid}/like                  | POST       |                  | Make the currently logged user like the post with uuid |
| /api/posts/{uuid}/unlike                | POST       |                  | Make the currently logged user unlike the post with uuid |
| /api/posts                              | POST       | [Post Content]()    | As the currently logged user, create a post with the content given in the request body |
| /api/posts/{uuid}/respond               | POST       | [Response Content]() | As the currently logged user, create a response with the content given in the request body, that responds to the post with uuid |
| /api/posts/{uuid}/quote               | POST       | [Quote Content]()  | As the currently logged user, create a quote with the content given in the request body, that references the post with uuid |
| /api/feed?skip&limit&since&by            | GET        |                  | Get the feed of the currently logged user. Parameter 'since' can be set to *HOUR/SIX_HOURS/TWELVE_HOURS*. Parameter 'by' set to 'POPULARITY' returns most popular posts in a given frame. By default parameters 'skip' and 'limit' are set to 0 and 20 respectively. Calling this endpoint with no parameters returns at most 20 most recent posts that have been posted in the last hour |

## Request bodies

### User details

* displayedUsername valid length is between 1 and 70 characters
* description valid length is between 1 and 200 characters

```JSON
{
  "displayedUsername": "",
  "description": "",
  "aviURL": ""
}
```

#### Post Content

* content valid length is between 1 and 300 characters

```JSON
{
  "content": ""
}
```

#### Quote Content

* content valid length is between 0 and 300 characters

```JSON
{
  "content": ""
}
```

#### Response Content

* content valid length is between 1 and 300 characters

```JSON
{
  "content": ""
}
```