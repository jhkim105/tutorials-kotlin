package jhkim105.tutorials.jpa.model.listener

object JpaListenerUtils {
    fun detectChangedFields(
        propertyNames: Array<String>,
        oldState: Array<Any?>?,
        newState: Array<Any?>?
    ): List<String> {
        if (oldState == null || newState == null) return emptyList()

        val changed = mutableListOf<String>()
        for (i in propertyNames.indices) {
            val oldVal = oldState[i]
            val newVal = newState[i]
            if (oldVal != newVal) {
                changed.add(propertyNames[i])
            }
        }
        return changed
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOldValue(
        propertyName: String,
        propertyNames: Array<String>,
        oldState: Array<Any?>?
    ): T? {
        val index = propertyNames.indexOf(propertyName)
        return if (index >= 0 && oldState != null) oldState[index] as? T else null
    }
}