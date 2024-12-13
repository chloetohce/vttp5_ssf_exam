package vttp.batch5.ssf.noticeboard.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import vttp.batch5.ssf.noticeboard.models.Notice;
import vttp.batch5.ssf.noticeboard.services.NoticeService;

// Use this class to write your request handlers

@Controller
@RequestMapping("")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    @GetMapping(path = {"", "/"})
    public ModelAndView landingPage(ModelAndView mav) {
        mav.setViewName("notice");
        mav.addObject("notice", new Notice());
        
        return mav;
    }

    @PostMapping("/notice")
    public ModelAndView postNotice(@Valid @ModelAttribute("notice") Notice notice, BindingResult binding, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("notice");

        if (binding.hasErrors()) {
            return mav;
        }

        try {
            String noticeId = noticeService.postToNoticeServer(notice);
            mav.setViewName("post-success");
            mav.addObject("noticeId", noticeId);
        } catch (Exception e) {
            mav.setViewName("post-error");
            mav.addObject("url", "\"" + request.getRequestURL().toString() + "\"");
            mav.addObject("errMsg", e.getMessage());
        }
        return mav;
    }

    @GetMapping("/healthz")
    public ModelAndView getHealthz() {
        ModelAndView mav = new ModelAndView("notice");

        try {
            if (noticeService.checkRepo() == null) {
                throw new Exception("Error accessing redis");
            }

            mav.setStatus(HttpStatusCode.valueOf(200));
            return mav;

        } catch (Exception e) {
            mav.setViewName("unhealthy");
            mav.setStatus(HttpStatusCode.valueOf(503));
            return mav;
        }
    }
    
}
