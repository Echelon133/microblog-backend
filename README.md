# Blobb

Simple twitter-like API.

## Features
* following other users
* posting blobbs (the equivalent of tweets)
* posting reblobbs (the equivalent of retweets)
* posting response blobbs (the equivalent of twitter responses)
* liking/unliking other users posts
* tagging posts with tags just by mentioning them in the post content
* retrieving most popular tags in the last hour/day/week
* getting user feed based on popularity/recency of posts 

## API

| Endpoint                                 | Method     | Request body     | Description                                      |
|------------------------                  |----------  |----------------- |-------------                                     |
| /api/users/me                            | GET        |                  | Get info about the user that is currently logged in |
| /api/users/me                            | PUT        | User details     | Update displyed username, description and profile picture of currently logged user |
| /api/users/{uuid}                        | GET        |                  | Get info about the user with uuid |
| /api/users/{uuid}/profile                | GET        |                  | Get counters that show how many follows/followers the user with uuid has |
| /api/users/{uuid}/follow                 | GET        |                  | Get a response that shows whether currently logged user follows the user with uuid |
| /api/users/{uuid}/follow                 | POST       |                  | Make currently logged user follow the user with uuid |
| /api/users/{uuid}/unfollow               | POST       |                  | Make currently logged user unfollow the user with uuid |
| /api/users/{uuid}/followers?skip&limit   | GET        |                  | Get a list of users that follow the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/follows?skip&limit     | GET        |                  | Get a list of users that are followed by the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/recentBlobbs?skip&limit| GET        |                  | Get a list of most recent blobbs of user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 10 respectively |
| /api/users?username                      | GET        |                  | Get the user with exact username as given in the parameter 'username'. This parameter is required |
| /api/tags?name                           | GET        |                  | Get the tag with exact name. The parameter 'name' is required |
| /api/tags/popular?since&limit            | GET        |                  | Get a list of tags popular within a certain frame. The parameter 'since' can be set to values: *HOUR/DAY/WEEK*. If not provided, 'since' is set to *HOUR* and 'limit' is set to 5 |
| /api/tags/{uuid}/recentBlobbs?skip&limit | GET        |                  | Get a list of most recent blobbs tagged with the tag with given uuid |                
| /api/blobbs/{uuid}                       | GET        |                  | Get the blobb with uuid |
| /api/blobbs/{uuid}/info                  | GET        |                  | Get blobb's response/like/reblobb counters |
| /api/blobbs/{uuid}                       | DELETE     |                  | Mark the blobb with uuid as deleted |
| /api/blobbs/{uuid}/responses?skip&limit  | GET        |                  | Get responses to the blobb with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/blobbs/{uuid}/reblobbs?skip&limit   | GET        |                  | Get reblobbs of the blobb with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/blobbs/{uuid}/like                  | GET        |                  | Get info whether the currently logged user likes the blobb with uuid |
| /api/blobbs/{uuid}/like                  | POST       |                  | Make the currently logged user like the blobb with uuid |
| /api/blobbs/{uuid}/unlike                | POST       |                  | Make the currently logged user unlike the blobb with uuid |
| /api/blobbs                              | POST       | Blobb Content    | As the currently logged user, create a blobb with the content given in the request body |
| /api/blobbs/{uuid}/respond               | POST       | Response Content | As the currently logged user, create a response with the content given in the request body, that responds to the blobb with uuid |
| /api/blobbs/{uuid}/reblobb               | POST       | Reblobb Content  | As the currently logged user, create a reblobb with the content given in the request body, that references the blobb with uuid |
| /api/feed?skip&limit&since&by            | GET        |                  | Get the feed of the currently logged user. Parameter 'since' can be set to *HOUR/SIX_HOURS/TWELVE_HOURS*. Parameter 'by' set to 'POPULARITY' returns most popular blobbs in a given frame. By default parameters 'skip' and 'limit' are set to 0 and 20 respectively. Calling this endpoint with no parameters returns at most 20 most recent blobbs that have been posted in the last hour |
