package kr.co.data_status_evaluation.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.Throwables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

@Slf4j
@Controller
public class CustomExceptionController implements ErrorController {
    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // get error status
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        log.error("ERRROR =======" + activeProfile);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (activeProfile.equals("dev")) {
                log.error("ERRROR =======" + activeProfile);
                Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
                String exceptionMessage = getExceptionMessage(throwable, statusCode);

                String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
                if (requestUri == null) {
                    requestUri = "Unknown";
                }
                String message = MessageFormat.format("{0} returned for {1} with message {2}",
                        statusCode, requestUri, exceptionMessage
                );
                model.addAttribute("statusCode", statusCode);
                model.addAttribute("errorMessage", message);

                return "exceptions/dev";
            }


            // display specific error page
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "exceptions/pageNotFound";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "exceptions/serviceNotAvailable";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "exceptions/accessDenied";
            }
        }

        // display generic error
        return "exceptions/serviceNotAvailable";
    }

    private String getExceptionMessage(Throwable throwable, Integer statusCode) {
        if (throwable != null) {
            return Throwables.getRootCause(throwable).getMessage();
        }
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        return httpStatus.getReasonPhrase();
    }
}
