package  com.emins_emrea.shareexperience.view

import java.io.Serializable

class Post (
    var userEmail : String,
    var title : String,
    var experienceDetail : String,
    var imageUrl : String,
    var categories : ArrayList<String>,
    var likes : ArrayList<String>,
    var likesCount: Long) : Serializable {

}