package com.example.trelloclone.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.activities.*
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class fireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity : SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()

            }.addOnFailureListener{
                e->
                Log.e(activity.javaClass.simpleName, "Error....")
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)

            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board...", e)
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Board created successfully...")

                Toast.makeText(activity,
                    "Board created successfully!...",
                    Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating a board...",
                    exception
                )

            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val boardList : ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardListToUI(boardList)
            }.addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating board...", e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Task List updated successfully...")

                if(activity is TaskListActivity)
                   activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()

            }.addOnFailureListener {
                    exception ->
                if(activity is TaskListActivity)
                  activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                  activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while creating a board...", exception)
            }

    }

    fun updateUserProfileData(activity: Activity,
                              userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "User Profile updated successfully...")

                Toast.makeText(activity,
                "Profile updated...",
                Toast.LENGTH_SHORT).show()

                when(activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                }



            }.addOnFailureListener {
                e ->
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }

                Log.i(activity.javaClass.simpleName, "Error while updating profile...")

                Toast.makeText(activity,
                    "Error while Profile updation...",
                    Toast.LENGTH_SHORT).show()
            }

    }

    fun LoadUserData(activity : Activity, readBoardList: Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener {document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity){
                    is SignInActivity ->{
                        if (loggedInUser != null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity ->{
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                    }
                    is MyProfileActivity ->{
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

            }.addOnFailureListener{
                    e->
                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error writing document..")
            }
    }

    fun getCurrentUserID():String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFireStore.collection(Constants.USERS)
            .whereIn(
                Constants.ID,
                assignedTo
            )
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MembersActivity) {
                    activity.setupMembersList(usersList)
                }else if (activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }
            }
            .addOnFailureListener { e ->
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }

            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

}