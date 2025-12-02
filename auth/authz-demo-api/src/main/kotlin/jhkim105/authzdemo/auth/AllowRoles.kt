package jhkim105.authzdemo.auth

import jhkim105.authzdemo.user.Role

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AllowRoles(vararg val value: Role)
