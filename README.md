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

| Endpoint                                 | Method     | Request body                    | Description                                      |
|------------------------                  |----------  |-----------------                |-------------                                     |
| /api/users/me                            | GET        |                                 | Get info about the user that is currently logged in |
| /api/users/me                            | PUT        | User details                    | Update displyed username, description and profile picture of currently logged user |
| /api/users/{uuid}                        | GET        |                                 | Get info about the user with uuid |
| /api/users/{uuid}/profile                | GET        |                                 | Get counters that show how many follows/followers the user with uuid has |
| /api/users/{uuid}/follow                 | GET        |                                 | Get a response that shows whether currently logged user follows the user with uuid |
| /api/users/{uuid}/follow                 | POST       |                                 | Make currently logged user follow the user with uuid |
| /api/users/{uuid}/unfollow               | POST       |                                 | Make currently logged user unfollow the user with uuid |
| /api/users/{uuid}/followers?skip&limit   | GET        |                                 | Get a list of users that follow the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/follows?skip&limit     | GET        |                                 | Get a list of users that are followed by the user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 5 respectively |
| /api/users/{uuid}/recentBlobbs?skip&limit| GET        |                                 | Get a list of most recent blobbs of user with uuid. Parameters 'skip' and 'limit' are optional. By default they are 0 and 10 respectively |
| /api/users?username                      | GET        |                                 | Get the user with exact username as given in the parameter 'username'. This parameter is required |

