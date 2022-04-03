package xyz.naotiki_apps.compose_kakeibo

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.composable

//いちいちこうかくのめんどいからえいりあす
typealias NavigateFunc = (route: String) -> Unit


 enum class KakeiboScreen(
    val body: @Composable (NavBackStackEntry, toNavigate: NavigateFunc) -> Unit,
    private val args: List<NamedNavArgument>? = null
) {
    Summary(
        { _, nav -> SummaryBody() { nav(AddItem.name + "/" + it) } }),
    AddItem(
        { it, nav ->
            AddItemBody(Date.dateFromInt(it.arguments?.getInt("date")!!), nav)
        },
        listOf(navArgument("date") {
            type = NavType.IntType
            defaultValue = Date.getToday().toInt()
        })
    ),
    Detail({ _: NavBackStackEntry, _: NavigateFunc ->
        DetailBody()
    }),
    SettingsScreen({ navBackStackEntry: NavBackStackEntry, function: NavigateFunc ->
        SettingsBody()
    }),
    CategorySettings({ _: NavBackStackEntry, _: NavigateFunc ->
        CategorySettingsBody()
    }) ;


    companion object {
        const val NAVIGATE_TO_BACK = "kakeibo_navigate_to_back"
        fun fromRoute(route: String): KakeiboScreen {
            return values().first {
                it.name == route.split("/")[0]
            }
        }

        fun NavGraphBuilder.buildGraph(toNavigate: (route: String) -> Unit) {

            values().forEach { screen ->
                var appendStr = ""
                if (screen.args != null) {
                    appendStr = buildString {
                        screen.args.forEach {

                            append("/")
                            append("{${it.name}}")
                        }
                    }
                }
                composable("${screen.name}$appendStr", arguments = screen.args.orEmpty()) { backStackEntry ->
                    screen.body(backStackEntry, toNavigate)
                }
            }

        }
    }
}

