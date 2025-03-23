package jhkim105.tutorials.redis.dlock.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

object KeyExtractor {
    private val parser: ExpressionParser = SpelExpressionParser()

    fun extractKey(joinPoint: ProceedingJoinPoint, keyExpression: String): String? {
        val signature = joinPoint.signature as MethodSignature
        val context = getStandardEvaluationContext(signature.parameterNames, joinPoint.args)
        return parser.parseExpression(keyExpression).getValue(context, String::class.java)
    }

    private fun getStandardEvaluationContext(parameterNames: Array<String>, args: Array<Any?>): StandardEvaluationContext {
        return StandardEvaluationContext().apply {
            parameterNames.forEachIndexed { index, name ->
                setVariable(name, args[index])
            }
        }
    }
}