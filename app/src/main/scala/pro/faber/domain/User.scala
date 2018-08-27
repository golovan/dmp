package pro.faber.domain

// https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/user-object
case class User(
                 id: Long,
                 name: String,
                 screenName: String,
                 location: Option[String],
                 url: Option[String],
                 description: Option[String],
                 followersCount: Long,
                 friendsCount: Long,
                 listedCount: Long,
                 favouritesCount: Long,
                 statusesCount: Long,
                 createdAt: String,
                 lang: Option[String],
                 profileImageUrl: Option[String]
               )
