package jhkim105.tutorials.jpa.event

object EntityChangeDetector {

    /**
     * 변경된 필드명을 반환합니다.
     * @param propertyNames 엔티티의 속성 이름 배열
     * @param oldState 변경 전 값 배열 (DB 기준)
     * @param newState 변경 후 값 배열 (엔티티 기준)
     * @return 변경된 필드 이름 목록
     */
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
}