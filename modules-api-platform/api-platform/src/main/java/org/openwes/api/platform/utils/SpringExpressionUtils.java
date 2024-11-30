package org.openwes.api.platform.utils;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpringExpressionUtils {

    private SpringExpressionUtils() {
        throw new IllegalStateException("ExpressionUtils is not initialized");
    }

    /**
     * 用于SpEL表达式解析.
     */
    private static final SpelExpressionParser parser = new SpelExpressionParser();
    /**
     * 用于获取方法参数定义名字.
     */
    private static final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    public static String generateKeyBySpEL(String spELString, JoinPoint joinPoint) {
        return String.valueOf(generateKeyBySpELObject(spELString, joinPoint));
    }

    public static Object generateKeyBySpELObject(String spELString, JoinPoint joinPoint) {
        if (StringUtils.isEmpty(spELString)) {
            return null;
        }
        // 通过joinPoint获取被注解方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 使用spring的DefaultParameterNameDiscoverer获取方法形参名数组
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        // 解析过后的Spring表达式对象
        Expression expression = parser.parseExpression(spELString);
        // spring的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 通过joinPoint获取被注解方法的形参
        Object[] args = joinPoint.getArgs();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            assert paramNames != null;
            context.setVariable(paramNames[i], args[i]);
        }
        return expression.getValue(context);
    }
}
