package com.ism.dissertation.controller;

import com.ism.dissertation.model.QuestionForm;
import com.ism.dissertation.model.Result;
import com.ism.dissertation.model.User;
import com.ism.dissertation.repository.UserRepo;
import com.ism.dissertation.service.FaceRecognitionService;
import com.ism.dissertation.service.QuizService;
import com.ism.dissertation.service.RegisterService;
import org.mindrot.jbcrypt.BCrypt;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.function.Consumer;

@Controller
public class MainController {

    @Autowired
    RegisterService registerService;
    @Autowired
    Result result;
    @Autowired
    QuizService qService;
    @Autowired
    UserRepo userRepository;
    @Autowired
    FaceRecognitionService faceRecognitionService;

    Boolean submitted = false;

    @ModelAttribute("result")
    public Result getResult() {
        return result;
    }

    @GetMapping("/")
    public String home() {
        return "index.html";
    }

    @GetMapping("/register")
    public String getRegister() {
        return "register.html";
    }

    @PostMapping("/quiz")
    public String quiz(@RequestParam String email, @RequestParam String password,
                       @RequestParam String photo,
                       Model m, RedirectAttributes ra) {
        if ((email.equals("") || password.equals("")) && photo.equals("")) {
            ra.addFlashAttribute("warning",
                    "You must enter your email and password, or login with face recognition");
            return "redirect:/";
        }

        submitted = false;

        if(!photo.equals("")){
            Integer integer = faceRecognitionService.faceRecognition(photo);
            if(integer == null){
                ra.addFlashAttribute("warning_photo",
                        "Face not recognized! Please take another photo or register!");
                return "redirect:/";
            } else {
                String username = userRepository.findUsernameById(integer);
                result.setUsername(username);
                QuestionForm qForm = qService.getQuestions();
                m.addAttribute("qForm", qForm);
                return "quiz.html";
            }
        }

        User user = userRepository.findByCredentials(email).get(0);
        if(BCrypt.checkpw(password,user.getPassword())){
            result.setUsername(user.getUsername());
            QuestionForm qForm = qService.getQuestions();
            m.addAttribute("qForm", qForm);
            return "quiz.html";
        } else {
            ra.addFlashAttribute("warning_credentials",
                    "Wrong username and password");
            return "redirect:/";
        }
    }

    @PostMapping("/submit")
    public String submit(@ModelAttribute QuestionForm qForm, Model m) {
        if (!submitted) {
            result.setTotalCorrect(qService.getResult(qForm));
            qService.saveScore(result);
            submitted = true;
        }

        return "result.html";
    }

    @GetMapping("/score")
    public String score(Model m) {
        List<Result> sList = qService.getTopScore();
        m.addAttribute("sList", sList);

        return "scoreboard.html";
    }

    @PostMapping("/registerUser")
    public String registerUser(@RequestParam String username, @RequestParam String password,
                               @RequestParam String email, @RequestParam String photo,
                               Model m, RedirectAttributes ra) {

        Integer userId = userRepository.findIdUserByEmail(email);
        if (userId != null) {
            ra.addFlashAttribute("warning_email","Email already in use! Please use another one!");
            return "redirect:/register";
        }

        userId = userRepository.findIdUserByUsername(username);
        if (userId != null) {
            ra.addFlashAttribute("warning_username","Email already in use! Please use another one!");
            return "redirect:/register";
        }


        if (!faceRecognitionService.hasFace(photo)) {
            ra.addFlashAttribute("warning_photo","No face detected! Please take another photo!");
            return "redirect:/register";
        }


        try{
            registerService.registerUser(username, password, email, photo);
            ra.addFlashAttribute("success", "The new user was successfully created ! ");
            return "redirect:/";
        }catch(Exception ex){
            ex.printStackTrace();
            ra.addFlashAttribute("warning", "An error has occurred ! ");
            return "redirect:/registerUser";
        }

    }




}
