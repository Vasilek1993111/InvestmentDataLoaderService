package com.example.InvestmentDataLoaderService.util;

import com.example.InvestmentDataLoaderService.enums.AllowedQueryParam;
import com.example.InvestmentDataLoaderService.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Утилитный класс для валидации GET параметров
 */
public class QueryParamValidator {
    
    /**
     * Валидирует GET параметры для акций
     * 
     * @throws ValidationException если переданы недопустимые параметры
     */
    public static void validateSharesParams() throws ValidationException {
        validateParams(AllowedQueryParam.getSharesParams(), "акций");
    }
    
    /**
     * Валидирует GET параметры для фьючерсов
     * 
     * @throws ValidationException если переданы недопустимые параметры
     */
    public static void validateFuturesParams() throws ValidationException {
        validateParams(AllowedQueryParam.getFuturesParams(), "фьючерсов");
    }
    
    /**
     * Валидирует GET параметры для индикативов
     * 
     * @throws ValidationException если переданы недопустимые параметры
     */
    public static void validateIndicativesParams() throws ValidationException {
        validateParams(AllowedQueryParam.getIndicativesParams(), "индикативов");
    }
    
    /**
     * Валидирует GET параметры против списка разрешенных
     * 
     * @param allowedParams массив разрешенных параметров
     * @param endpointName название эндпоинта для сообщения об ошибке
     * @throws ValidationException если переданы недопустимые параметры
     */
    private static void validateParams(String[] allowedParams, String endpointName) throws ValidationException {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return; // Если нет контекста запроса, пропускаем валидацию
        }
        
        Set<String> allowedSet = new HashSet<>();
        for (String param : allowedParams) {
            allowedSet.add(param.toLowerCase());
        }
        
        Set<String> invalidParams = new HashSet<>();
        Enumeration<String> paramNames = request.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (!allowedSet.contains(paramName.toLowerCase())) {
                invalidParams.add(paramName);
            }
        }
        
        if (!invalidParams.isEmpty()) {
            throw new ValidationException(
                "Недопустимые параметры для " + endpointName + ": " + String.join(", ", invalidParams) + 
                ". Разрешенные параметры: " + String.join(", ", allowedParams),
                "queryParams"
            );
        }
    }
    
    /**
     * Получает текущий HTTP запрос
     * 
     * @return HttpServletRequest или null если контекст недоступен
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
