package com.msa.mynotes.models

class Labels(var labelId: String, var userMobile: String,
             var name: String, var about: String, var date: String){

    companion object{
        // عشان احدد لما يطلع الLabelDialog اذا كان السبب إضافة label ولا ضغط على التعديل وبده يعدل label
        var type = "newLabel"

        // عشان اذا ضاف label جديدة يحدث الrecyclerView
        var fragment = "LabelsFragment"
    }
}