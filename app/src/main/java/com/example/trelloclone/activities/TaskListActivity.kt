package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.TaskListItemsAdapter
import com.example.trelloclone.firebase.fireStoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.Card
import com.example.trelloclone.models.Task
import com.example.trelloclone.models.User
import com.example.trelloclone.utils.Constants
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mboardDocumentId : String
    lateinit var mAssignedMembersDetailList : ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID)) {
            mboardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

            showProgressDialog(resources.getString(R.string.please_wait))
            fireStoreClass().getBoardDetails(this, mboardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK  && requestCode == MEMBERS_REQUEST_CODE ||
                                                requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            fireStoreClass().getBoardDetails(this, mboardDocumentId)
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun boardDetails(board: Board){
        mBoardDetails = board

        hideProgressDialog()
        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().getAssignedMembersListDetails(this@TaskListActivity, mBoardDetails.assignedTo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpActionBar(){
        setSupportActionBar(toolbar_task_list_activity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        toolbar_task_list_activity.setNavigationOnClickListener(){
            onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().getBoardDetails(this, mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, fireStoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(fireStoreClass().getCurrentUserID())

        val card = Card(cardName, fireStoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    fun boardMembersDetailList(list: ArrayList<User>) {
        mAssignedMembersDetailList = list

        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_task_list.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this@TaskListActivity, mBoardDetails.taskList)
        rv_task_list.adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        fireStoreClass().addUpdateTaskList(this@TaskListActivity, mBoardDetails)
    }

    companion object{
        const val MEMBERS_REQUEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }


}