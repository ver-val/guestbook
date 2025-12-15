package com.example.guestbook.web.error;

import com.example.guestbook.core.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class MvcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ModelAndView handleNotFound(Exception ex) {
        log.debug("Not found: {}", ex.getMessage());
        ModelAndView mv = new ModelAndView("error/404");
        mv.setStatus(HttpStatus.NOT_FOUND);
        mv.addObject("errorMessage", ex.getMessage());
        return mv;
    }
}
