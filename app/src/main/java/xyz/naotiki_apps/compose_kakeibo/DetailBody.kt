package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.parseAllCategoriesWithChildren
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val itemDataRepository: ItemDataRepository,
) : ViewModel() {
    var allCategories = mutableStateOf<List<Category>?>(null)

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().stateIn(
                viewModelScope,
                WhileSubscribed(), null
            ).collect {
                allCategories.value = it
            }
        }
    }

    fun generateCategoryIcon(itemData: ItemData): CategoryData {
        val category = allCategories.value.orEmpty().firstOrNull { it.id == itemData.categoryId }
        val parentCategory = allCategories.value.orEmpty().firstOrNull { category?.parentId == it.id }
        val iconText = category?.iconText?.ifEmpty { parentCategory?.iconText }.orEmpty()
        val color = category?.color ?: parentCategory?.color
        return Triple(
            iconText,
            color,
            parentCategory?.name?.let { "$it > " }.orEmpty() + category?.name
        )
    }

    fun removeItemData(itemDataList: List<ItemData>) = itemDataRepository.removeItemData(itemDataList)

    fun recoveryItemData(cathe: Array<ItemData>) = itemDataRepository.insertItemData(*cathe)
    var itemDataFlow by mutableStateOf<StateFlow<List<ItemData>>?>(null)
    fun getItemData(
        dateRange: DateRange,
        sort: ItemDataSort,
        isDescending: Boolean,
        searchText: String,
        categoryIds: List<Int>,
    ) {
        itemDataFlow = itemDataRepository.getItemData(dateRange, categoryIds, sort, isDescending, searchText).stateIn(
            viewModelScope,
            WhileSubscribed(), emptyList()
        )
    }

    fun updateItemData(itemData: List<ItemData>) = itemDataRepository.updateItemData(itemData)
}


@Composable
fun DetailBody(detailViewModel: DetailViewModel = hiltViewModel()) {

    val scaffoldState = rememberScaffoldState()
    val coroutine = rememberCoroutineScope()
    val categories by detailViewModel.allCategories
    var editMode by remember { mutableStateOf(false) }
    val selectedItemDataList = remember(editMode) { mutableStateListOf<ItemData>() }
    var deleteDialog by remember { mutableStateOf(false) }
    if (deleteDialog) {
        AlertDialog({ deleteDialog = false }, confirmButton = {
            Button({
                //コンポジションを離れてもキャンセルしない
                ioThread {
                    val cache = selectedItemDataList.toTypedArray()
                    detailViewModel.removeItemData(selectedItemDataList)
                    selectedItemDataList.clear()
                    when (scaffoldState.snackbarHostState.showSnackbar("削除しました", "元に戻す", SnackbarDuration.Long)) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed -> {
                            detailViewModel.recoveryItemData(cache)
                        }
                    }
                }
                deleteDialog = false
            }) {
                Text("削除")
            }
        }, dismissButton = {
            TextButton({ deleteDialog = false }) {
                Text("キャンセル")
            }
        }, title = { Text("データ削除") }, text = {
            Text("${selectedItemDataList.size}件のデータを削除します。")
        })
    }


    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(if (!editMode) "詳細" else "${selectedItemDataList.size}個選択中") },
            navigationIcon = if (editMode) {
                {
                    IconButton({ editMode = false }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            } else null,
            actions = {
                if (!editMode) {
                    IconButton({ editMode = true }) {
                        Icon(Icons.Default.Edit, null)
                    }
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton({ expanded = selectedItemDataList.isNotEmpty() }) {
                        Icon(Icons.Default.Category, null)
                    }
                    CategoriesDropDown(expanded, { expanded = false }, categories.orEmpty()) { category: Category ->
                        expanded = false
                        selectedItemDataList.forEach {
                            it.categoryId = category.id
                        }
                        ioThread {
                            detailViewModel.updateItemData(selectedItemDataList)
                            selectedItemDataList.clear()
                        }
                    }

                    IconButton({
                        deleteDialog = selectedItemDataList.isNotEmpty()
                    }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }

            })
        var searchText by remember { mutableStateOf("") }
        TextField(
            searchText,
            { searchText = it },
            singleLine = true,
            label = { Text("検索") },
            leadingIcon = { Icon(Icons.Default.Search, null) })

        val selectedCategoryIds = remember { mutableStateListOf<Int>() }
        var sortWay by remember { mutableStateOf(ItemDataSort.Date) }
        var isDescending by remember { mutableStateOf(false) }
        Row {
            var expandedCategory by remember { mutableStateOf(false) }
            OutlinedButton({ expandedCategory = true }) {
                Icon(if (selectedCategoryIds.isEmpty()) Icons.Outlined.Category else Icons.Filled.Category, null)
                Text("カテゴリー")
            }
            DropdownMenu(expandedCategory, { expandedCategory = false }) {
                categories.orEmpty().parseAllCategoriesWithChildren().forEach { (parent, children) ->
                    val hasChildren = children != null
                    val triState = children?.run {
                        if (selectedCategoryIds.containsAll(children.map { it.id })) ToggleableState.On
                        else if (selectedCategoryIds.any { it in children.map { c -> c.id } }) ToggleableState.Indeterminate
                        else ToggleableState.Off
                    }
                    DropdownMenuItem({
                        if (hasChildren) {
                            if (triState != ToggleableState.On) selectedCategoryIds.addAsSet(children!!.map { it.id })
                            else selectedCategoryIds.removeAll(children.map { it.id })
                        } else {
                            if (selectedCategoryIds.contains(parent.id)) {
                                selectedCategoryIds.remove(parent.id)
                            } else {
                                selectedCategoryIds.addAsSet(parent.id)
                            }
                        }
                    }) {
                        if (hasChildren) {

                            TriStateCheckbox(triState!!, {
                                if (triState != ToggleableState.On) selectedCategoryIds.addAsSet(children.map { it.id })
                                else selectedCategoryIds.removeAll(children.map { it.id })
                            })
                        } else {
                            Checkbox(selectedCategoryIds.contains(parent.id), {
                                if (!it)
                                    selectedCategoryIds.remove(parent.id)
                                else selectedCategoryIds.addAsSet(parent.id)
                            })
                        }

                        Text(parent.name)
                    }
                    if (hasChildren) {
                        Divider()
                        children?.forEach { child ->
                            DropdownMenuItem({
                                if (selectedCategoryIds.contains(child.id))
                                    selectedCategoryIds.remove(child.id)
                                else selectedCategoryIds.addAsSet(child.id)
                            }) {
                                Spacer(Modifier.width(10.dp))
                                Checkbox(selectedCategoryIds.contains(child.id), {
                                    if (!it)
                                        selectedCategoryIds.remove(child.id)
                                    else selectedCategoryIds.addAsSet(child.id)
                                })
                                Text(child.name)
                            }
                        }
                        Divider()
                    }
                }
            }

            var expandedSort by remember { mutableStateOf(false) }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton({
                        expandedSort = true
                    }) {
                        Icon(Icons.Default.Sort, null)
                        Text(sortWay.sortName)
                    }
                    Text(if (isDescending) "${sortWay.sortExample.second}→${sortWay.sortExample.first}" else "${sortWay.sortExample.first}→${sortWay.sortExample.second}")
                    Switch(isDescending, { isDescending = it })
                }
                DropdownMenu(expandedSort, { expandedSort = false }) {
                    ItemDataSort.values().forEach {
                        DropdownMenuItem({ sortWay = it;expandedSort = false }) {
                            Text(it.sortName)
                        }
                    }
                }
            }
        }
        val itemDataList = detailViewModel.itemDataFlow?.collectAsState()
        LaunchedEffect(searchText, sortWay, selectedCategoryIds.toList(), isDescending) {
            detailViewModel.getItemData(
                DateRange(Date(2022, 3)),
                sortWay,
                isDescending,
                searchText,
                selectedCategoryIds
            )
        }
        LazyColumn() {
            items(itemDataList?.value.orEmpty(), { it.id }) { item ->
                val selected = item in selectedItemDataList
                Row(modifier = Modifier.clickable {
                    if (selected) selectedItemDataList.remove(item)
                    else selectedItemDataList.addAsSet(item)
                }, verticalAlignment = Alignment.CenterVertically) {
                    if (editMode) Checkbox(selected, { v ->
                        if (!v) selectedItemDataList.remove(item)
                        else selectedItemDataList.addAsSet(item)
                    })
                    ItemDataElement(item, detailViewModel.generateCategoryIcon(item))
                }
                Divider()
            }
        }
    }

}

typealias CategoryData = Triple<String, ColorData?, String>

@Composable
fun ItemDataElement(itemData: ItemData, categoryIcon: CategoryData) {

    Column(Modifier.padding(5.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            CategoryIcon(categoryIcon.first, categoryIcon.second)
            Spacer(Modifier.width(5.dp))
            Text(categoryIcon.third, color = Color.Gray, style = MaterialTheme.typography.h6)
        }
        Text(itemData.name, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.h5)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(itemData.date.toString(), fontSize = 18.sp)
            Text("${itemData.price}円", fontSize = 18.sp)
        }
    }


}


@Composable
fun CategoryIcon(text: String = "", colorData: ColorData?) {
    val color = colorData?.toColor() ?: Color.Transparent
    val contentColor = color.generateContentColor()
    if (text.isNotEmpty() || color != Color.Transparent) {
        Box(modifier = Modifier.fillMaxHeight().background(color, CircleShape)) {
            // Optimize "on"colors
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                if (text.isNotEmpty())
                    Text(
                        text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 5.dp),
                    )
                else Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun CategoriesDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    categories: List<Category>,
    onSelected: (category: Category) -> Unit,
) {
    val categoriesWithChildren = categories.parseAllCategoriesWithChildren()
    val currentOnSelected by rememberUpdatedState(onSelected)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        categoriesWithChildren.forEach { (parent, children) ->
            val hasChildren = children != null
            if (hasChildren) {
                Text(parent.name, Modifier.padding(5.dp))
                Divider()
                children!!.forEach {
                    DropdownMenuItem({
                        currentOnSelected(it)
                    }) { Spacer(Modifier.width(10.dp)); Text(it.name) }
                }
                Divider()

            } else {
                DropdownMenuItem({
                    currentOnSelected(parent)
                }) {
                    Text(parent.name)
                }
            }
        }
    }
}
