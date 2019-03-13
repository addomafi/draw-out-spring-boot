package br.com.ideotech.drawout.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public class ReflectionUtils {

	public static Object getParameterByAnnotationClass(Object[] args, java.lang.reflect.Method method, Class<?> annotationClass) {
		Parameter[] params = method.getParameters();
		Class<?> requestBodyType = null;
		for (Parameter param : params) {
			for (Annotation annotation : param.getAnnotations()) {
				if (annotation.annotationType().equals(org.springframework.web.bind.annotation.RequestBody.class)) {
					requestBodyType = param.getType();
				}
			}
		}
		
		if (requestBodyType != null) {
			for (Object arg : args) {
				if (arg.getClass().equals(requestBodyType)) {
					return arg;
				}
			}
		}
		
		return null;
	}
}
