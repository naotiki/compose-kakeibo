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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.Yellow
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
    val categoryRepository: CategoryRepository,
) : ViewModel() {
    val allCategories = categoryRepository.getAllCategories().stateIn(viewModelScope, WhileSubscribed(), emptyList())
    fun updateCategory(category: Category) {
    }
}


@Composable
fun CategorySettingsBody(categorySettingsViewModel: CategorySettingsViewModel = hiltViewModel()) {
    val allCategories by categorySettingsViewModel.allCategories.collectAsState()
    val ac = allCategories.filter { it.id != 1 }.parseAllCategoriesWithChildren()
    val scrollState = rememberScrollState()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var cache by remember { mutableStateOf<Category?>(null) }
    val hasCache = cache != null
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TopAppBar(title = {
            Text("カテゴリー")
        }, actions = {
            IconButton({
                TODO("追加処理")
                //保存前にDBにつくるかどうするか
                //なやみどころー
            }, modifier = Modifier, enabled = selectedCategory?.parentId == null) {
                Icon(Icons.Default.Add, null)
            }
            IconButton({
                TODO("削除前に使用箇所を検索して、あればアラートを出す")
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
                    Text(parent.name, style = MaterialTheme.typography.h6)
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
                            Text(child.name, style = MaterialTheme.typography.h6)
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
                TextField(name, { name = it }, label = { Text("カテゴリー名") })
                var selectedColor by remember(selectedCategory) { mutableStateOf(selectedCategory!!.color) }

                Text("色")
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
                                            Icon(Icons.Default.Check, null, Modifier.align(Alignment.Center))
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                Text("アイコンテキスト")
                var iconText by remember(selectedCategory) { mutableStateOf(selectedCategory!!.iconText) }
                TextField(iconText?.iconText ?: "", onValueChange = {
                    if (it.codePoints().count() == 1L && it.isNotBlank()) {
                        iconText = IconText(it)
                    } else if (it.isEmpty()) {
                        iconText = null
                    }
                }, placeholder = { Text("絵文字など一文字") }, singleLine = true)

                Text("表示サンプル")
                Text("色やアイコンを指定していなくても親カテゴリに指定されている場合は、親の色やアイコンが適用されます。", style = MaterialTheme.typography.caption)
                val parentCategory = allCategories.firstOrNull { selectedCategory!!.parentId == it.id }
                ItemDataElement(
                    exampleItemData,
                    CategoryData(
                        iconText ?: parentCategory?.iconText, selectedColor ?: parentCategory?.color,
                        parentCategory?.name?.let { "$it > " }.orEmpty() + selectedCategory!!.name
                    )
                )
                //変更チェック
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
                    TODO("保存")
                }, enabled = hasCache, modifier = Modifier.align(Alignment.End)) {
                    Text("保存")
                }
            } else Text("カテゴリを選択してください")

        }
    }

}


val exampleItemData = ItemData(name = "サンプルデータ", date = Date.getToday(), price = 110, categoryId = 0)
val colors: List<Color> = listOf(Transparent, LightGray, Red, Magenta, Blue, Cyan, Green, Yellow)

