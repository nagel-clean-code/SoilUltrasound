package ru.sem.soilultrasound.navigator

import ru.sem.soilultrasound.MainActivity


typealias MainActivityAction = (MainActivity) -> Unit

class MainActivityActions {
    var mainActivity: MainActivity? = null
        set(activity) {
            field = activity
            if (activity != null) {
                actions.forEach { it(activity) }
                clear()
            }
        }
    private val actions = mutableListOf<MainActivityAction>()

    operator fun invoke(action: MainActivityAction){
        if(mainActivity == null){
            actions += action
        }else{
            action.invoke(mainActivity!!)
        }
    }

    fun clear() {
        actions.clear()
    }
}