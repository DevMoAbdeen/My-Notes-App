package com.msa.mynotes.models

data class Notes(var noteId: String, var userMobile: String, var title: String, var description: String,
                 var createdAt: String, var updatedAt: String, var deletedAt: String,
                 var inArchives: Int, var isFixed: Int, var isDeleted: Int,
                 var color: String, var labelId: String){

    companion object{
        var color = "#FFFFFF"
        var labelId = "Without Label"
    }
}
