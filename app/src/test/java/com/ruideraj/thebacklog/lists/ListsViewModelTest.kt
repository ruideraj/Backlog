package com.ruideraj.thebacklog.lists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.ListsRepository
import com.ruideraj.backlog.data.local.ListItem
import com.ruideraj.backlog.lists.ListsViewModel
import com.ruideraj.thebacklog.MainDispatcherRule
import com.ruideraj.thebacklog.data.FakeListsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ListsViewModelTest {
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainDispatcherRule()

    private val listCount = 5
    private val testLists = (1..listCount).map {
        val icon = it % ListIcon.values().size
        ListItem(BacklogList(it.toLong(), "List $it", ListIcon.values()[icon], it.toDouble()), it)
    }

    private lateinit var testRepository: ListsRepository

    private lateinit var viewModel: ListsViewModel

    @Before
    fun setup() {
        testRepository = FakeListsRepository().apply { addLists(testLists) }
        viewModel = ListsViewModel(testRepository)
    }

    @Test
    fun init_listsLoaded() {
        assertThat(viewModel.lists.value, IsEqual(testLists))
    }

    @Test
    fun createList_correctEventSent() = runTest {
        viewModel.onClickCreateList()

        val result = viewModel.eventFlow.first()
        assertThat(result, IsInstanceOf(ListsViewModel.Event.ShowCreateList::class.java))
    }

    @Test
    fun editList_correctEventAndParameters() = runTest {
        testLists.forEachIndexed { i, listItem ->
            viewModel.onClickEditList(i)

            val result = viewModel.eventFlow.first() as ListsViewModel.Event.ShowEditList
            assertThat(result.listId, `is`(listItem.list.id))
            assertThat(result.icon, `is`(listItem.list.icon))
            assertThat(result.title, `is`(listItem.list.title))
        }
    }

    @Test
    fun deleteList_correctEventSent() = runTest {
        testLists.forEachIndexed { i, listItem ->
            viewModel.onClickDeleteList(i)

            val result = viewModel.eventFlow.first() as ListsViewModel.Event.ShowDeleteDialog
            assertThat(result.list, `is`(listItem.list))
        }
    }

    @Test
    fun clickList_correctEventSent() = runTest {
        testLists.forEachIndexed { i, listItem ->
            viewModel.onClickList(i)

            val result = viewModel.eventFlow.first() as ListsViewModel.Event.GoToEntries
            assertThat(result.list, `is`(listItem.list))
        }
    }

    @Test
    fun createList_emptyTitle_dialogErrorShown() {
        assertThat(viewModel.showListDialogTitleError.value, `is`(false))
        viewModel.createList("", ListIcon.LIST)
        assertThat(viewModel.showListDialogTitleError.value, `is`(true))
    }

    @Test
    fun createList_titleEntered_dialogClosedAndListCreated() = runTest {
        val title = "New List"
        val icon = ListIcon.GAME

        viewModel.createList(title, icon)

        assertThat(viewModel.eventFlow.first(), `is`(ListsViewModel.Event.CloseListDialog))
        assertThat(viewModel.lists.value, not(nullValue()))
        assertThat(viewModel.lists.value?.last()?.list?.title, `is`(title))
        assertThat(viewModel.lists.value?.last()?.list?.icon, `is`(icon))
    }

    @Test
    fun editList_blankTitle_dialogErrorShown() {
        val listToEdit = testLists[2].list

        assertThat(viewModel.showListDialogTitleError.value, `is`(false))
        viewModel.editList(listToEdit.id, "", listToEdit.icon)
        assertThat(viewModel.showListDialogTitleError.value, `is`(true))
    }

    @Test
    fun editList_changesSuccessful() {
        val icons = ListIcon.values()
        testLists.forEachIndexed { i, listItem ->
            val listId = listItem.list.id
            val newTitle = "New Title $i"
            val newIcon = icons[(listItem.list.icon.ordinal + 1) % icons.size]

            viewModel.editList(listId, newTitle, newIcon)

            assertThat(viewModel.lists.value?.get(i)?.list?.title, `is`(newTitle))
            assertThat(viewModel.lists.value?.get(i)?.list?.icon, `is`(newIcon))
        }
    }

    @Test
    fun deleteList_listDeleted() {
        testLists.forEach { listItem ->
            val listId = listItem.list.id

            viewModel.deleteList(listId)
            val lists = viewModel.lists.value

            assertThat(lists, not(nullValue()))
            assertThat(lists?.find { it.list.id == listId }, IsNull())
        }
    }

}