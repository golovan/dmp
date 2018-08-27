package pro.faber.domain

// https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object
case class Tweet(
                  createdAt: String,
                  id: Long,
                  text: String,
                  source: String,
                  device: Option[String],
                  lang: Option[String],
                  user: User
                )
