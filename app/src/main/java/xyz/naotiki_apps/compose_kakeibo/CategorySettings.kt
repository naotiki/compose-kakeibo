package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import xyz.naotiki_apps.compose_kakeibo.Category.Companion.parseAllCategoriesWithChildren
import xyz.naotiki_apps.compose_kakeibo.ColorData.Companion.toColorData
import javax.inject.Inject

@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val itemDataRepository: ItemDataRepository
) : ViewModel() {
    val allCategories = categoryRepository.getAllCategories().stateIn(viewModelScope, WhileSubscribed(), emptyList())
    fun updateCategory(category: Category) = categoryRepository.updateCategory(category)
    fun deleteCategory(category: Category)=categoryRepository.deleteCategory(category)
    fun existsCategoryChildItemData(category: Category):Boolean=itemDataRepository.existsItemDataByCategory(category)
}
@Composable
fun CategorySettingsBody(categorySettingsViewModel: CategorySettingsViewModel = hiltViewModel()) {
    val allCategories by categorySettingsViewModel.allCategories.collectAsState()
    val ac = allCategories.filter { it.id != 1 }.parseAllCategoriesWithChildren()
    val scrollState = rememberScrollState()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var cache by remember { mutableStateOf<Category?>(null) }
    val hasCache = cache != null
    //selectedCategory????????????????????????????????????????????????
    LaunchedEffect(allCategories){
        selectedCategory?.also {
            if (it !in allCategories) selectedCategory=null
        }
    }
    var beforeDeleteAlert by remember { mutableStateOf(false) }
    if (beforeDeleteAlert) {
        AlertDialog({ beforeDeleteAlert = false }, confirmButton = {
            Button({
                beforeDeleteAlert = false
                categorySettingsViewModel.deleteCategory(selectedCategory!!)
                selectedCategory=null
            }) {
                Text("??????")
            }
        }, dismissButton = {
            TextButton({ beforeDeleteAlert = false }) {
                Text("???????????????")
            }
        }, title = {
            Text("????????????????????????????????????????????????")
        }, text = {
            Text("???????????????????????????????????????????????????????????????????????????????????????????????????????????????")
        })
    }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TopAppBar(title = {
            Text("???????????????")
        }, actions = {
            IconButton({
                TODO("????????????")
                //????????????DB??????????????????????????????
                //?????????????????????
            }, modifier = Modifier, enabled = selectedCategory?.parentId == null) {
                Icon(Icons.Default.Add, null)
            }
            IconButton({
                TODO("????????????????????????????????????????????????????????????????????????")
            }, modifier = Modifier, enabled = selectedCategory != null) {
                Icon(Icons.Default.Remove, null)
            }
        })
        Column(
            Modifier.fillMaxWidth().height(250.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            ac.forEach { (parent, children) ->
                val hasChildren = children != null
                Row(
                    Modifier.fillMaxSize()
                        .toggleable(
                            selectedCategory == parent,
                            onValueChange = { selectedCategory = if (it) parent else null }).then(
                            if (selectedCategory == parent) Modifier.background(LightGray) else Modifier
                        ).padding(7.dp).height(IntrinsicSize.Min)
                ) {
                    CategoryIcon(parent.iconText, parent.color)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        parent.name,
                        style = MaterialTheme.typography.h6,
                        color = if (selectedCategory == parent) Black else Unspecified
                    )
                }
                if (hasChildren) {
                    Divider()
                    children!!.forEach { child: Category ->
                        Row(
                            Modifier.fillMaxSize()
                                .toggleable(
                                    selectedCategory == child,
                                    onValueChange = { selectedCategory = if (it) child else null }).then(
                                    if (selectedCategory == child) Modifier.background(LightGray) else Modifier
                                ).padding(7.dp).height(IntrinsicSize.Min)
                        ) {
                            Spacer(Modifier.width(20.dp))
                            CategoryIcon(child.iconText, child.color)
                            Text(
                                child.name,
                                style = MaterialTheme.typography.h6,
                                color = if (selectedCategory == child) Black else Unspecified
                            )
                        }
                    }
                    Divider()
                }
            }
        }
        Divider()
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (selectedCategory != null) {
                var name by remember(selectedCategory) { mutableStateOf(selectedCategory!!.name) }
                TextField(name, { name = it }, label = { Text("??????????????????") })
                var selectedColor by remember(selectedCategory) { mutableStateOf(selectedCategory!!.color) }

                Text("???")
                val colorListScroll = rememberScrollState()
                Row(Modifier.horizontalScroll(colorListScroll), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val selectedBorderColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)

                    colors.forEach { color ->
                        when (color) {
                            Transparent -> {
                                Box(Modifier.size(40.dp).background(color, CircleShape)
                                    .border(
                                        2.dp, if (selectedColor == null) selectedBorderColor else Gray,
                                        CircleShape
                                    ).clickable {
                                        selectedColor = null
                                    }) {
                                    Icon(Icons.Default.Clear, null, Modifier.align(Alignment.Center))
                                }
                            }
                            else -> {
                                Box(Modifier.size(40.dp).background(color, CircleShape)
                                    .border(
                                        2.dp,
                                        if (selectedColor?.colorInt == color.toArgb()) selectedBorderColor else Gray,
                                        CircleShape
                                    ).clickable {
                                        selectedColor = color.toColorData()
                                    }) {
                                    if (selectedColor?.colorInt == color.toArgb()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0x3F000000), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                null,
                                                Modifier.align(Alignment.Center),
                                                tint = White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                Text("????????????????????????")
                val emojiUtil = EmojiUtil.getInstance()
                var iconText by remember(selectedCategory) { mutableStateOf(selectedCategory!!.iconText) }
                val focusRequester = remember { FocusRequester() }
                val emojiValidate = emojiUtil.characterCount(iconText) <= 1
                TextField(
                    iconText,
                    modifier = Modifier.focusRequester(focusRequester),
                    onValueChange = {
                        iconText = it.also {
                            if (emojiUtil.characterCount(it) <= 1) focusRequester.freeFocus() else focusRequester.captureFocus()
                        }
                    },
                    label = { Text("????????????????????????(???????????????)") },
                    placeholder = { Text("????????????????????????(????????????)") },
                    singleLine = true,
                    isError = !emojiValidate
                )

                Text("??????????????????")
                Text("???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????", style = MaterialTheme.typography.caption)
                val parentCategory = allCategories.firstOrNull { selectedCategory!!.parentId == it.id }
                ItemDataElement(
                    exampleItemData,
                    CategoryData(
                        iconText.ifEmpty { parentCategory?.iconText }.orEmpty(), selectedColor ?: parentCategory?.color,
                        parentCategory?.name?.let { "$it > " }.orEmpty() + name
                    )
                )
                //??????????????????
                LaunchedEffect(name, selectedColor, iconText) {
                    val changed =
                        selectedCategory!!.copy(
                            name = name,
                            color = selectedColor,
                            iconText = iconText,
                        )
                    cache = if (selectedCategory != changed) {
                        changed
                    } else {
                        null
                    }
                }
                Button({
                    categorySettingsViewModel.updateCategory(cache!!)
                    //????????????????????????????????????
                    cache = null
                }, enabled = hasCache, modifier = Modifier.align(Alignment.End)) {
                    Text("??????")
                }
            } else {
                Text("??????????????????????????????????????????", style = MaterialTheme.typography.h5)
                Spacer(Modifier.height(15.dp))
                Row {
                    Text("?????????")
                    Icon(Icons.Default.Add, null)
                    Text("???????????????????????????????????????????????????")
                }
                Row {
                    Text("?????????")
                    Icon(Icons.Default.Remove, null)
                    Text("???????????????????????????????????????????????????")
                }
            }

        }
    }

}


val exampleItemData = ItemData(name = "?????????????????????", date = Date.getToday(), price = 110, categoryId = 0)

