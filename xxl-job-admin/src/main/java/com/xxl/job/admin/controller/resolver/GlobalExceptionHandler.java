package com.xxl.job.admin.controller.resolver;

import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ReturnT<String> valid(MethodArgumentNotValidException e) {
        logger.error("MethodArgumentNotValidException:", e);
        return ReturnT.fail(e.getBindingResult().getFieldErrors().stream().map(each -> each.getField() + each.getDefaultMessage()).collect(Collectors.joining(",")));
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView doError(Exception e, HandlerMethod method, HttpServletResponse response) {
        logger.error("Exception:{}", e);
        boolean isJson = Optional.ofNullable(method.getMethodAnnotation(ResponseBody.class)).isPresent();
        ModelAndView mv = new ModelAndView();
        if (isJson) {
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JacksonUtil.writeValueAsString(ReturnT.fail(e.getMessage())));
            } catch (IOException exception) {
                logger.error("IOException:{}", e);
            }
            return mv;
        } else {
            mv.addObject("exceptionMsg", e.getMessage());
            mv.setViewName("/common/common.exception");
            return mv;
        }
    }
}
