package com.example.demo.controllers;

import com.cloudinary.utils.ObjectUtils;
import com.example.demo.config.CloudinaryConfig;
import com.example.demo.models.Message;
import com.example.demo.models.User;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CloudinaryConfig cloudc;


    @Autowired
    private UserService userService;



    @RequestMapping("/")
    public String listCourses(Model model){
        model.addAttribute("messages", messageRepository.findAll());
        return "list";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }




    @RequestMapping(value="/register", method= RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping(value="/register", method= RequestMethod.POST)
    public String processRegistrationPage(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model){
        model.addAttribute("user", user);
        if (result.hasErrors()){
            return "registration";
        }else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Successfully Created");
        }
        return "index";
    }



//    // Lesson 23 modifications:
//    @RequestMapping("/secure")
//    public String secure(HttpServletRequest request,
//                         Authentication authentication,
//                         Principal principal){
//
//        Boolean isAdmin = request.isUserInRole("ADMIN");
//        Boolean isUser = request.isUserInRole("USER");
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        String username = principal.getName();
//
//        return "secure";
//    }


//    @GetMapping("/displayPersonAllInfo")
//    public String showAllPersonsInfo(Model model, Principal principal)
//    {
//        //  System.out.println("displayPersonAllInfo page: p.getname:"+ principal.getName());
//        Person myPerson = personRepository.findByUsername(principal.getName());
//        //  System.out.println("p.getname:"+ principal.getName());
//        model.addAttribute("person", myPerson );
//
//        long countAllSkills = skillsRepository.count();
//        model.addAttribute("gotskills", countAllSkills);
//
//        return "displayPersonAllInfo";
//    }






    @GetMapping("/add")
    public String messageForm(Model model, Principal principal){


        System.out.println("display user username: " + principal.getName());
        User myUser = userRepository.findByUsername(principal.getName());
//        model.addAttribute("user", myUser );

        String username = myUser.getUsername();
        model.addAttribute("username", username );
        Message myMessage = new Message();

        myMessage.setSentby(username);
        model.addAttribute("message", myMessage);

        return "messageform";
    }


//    @PostMapping("/process")
//    public String processForm(@Valid Message message, BindingResult result)
//    {
//        if (result.hasErrors()){
//            return "messageform";
//        }
//        messageRepository.save(message);
//        return "redirect:/";
//    }



    @PostMapping("/process")
    public String processForm(@Valid  @ModelAttribute ("message") Message message,  BindingResult result,
                          @RequestParam("file")MultipartFile file, Principal principal, Model model)
    {
        User myUser = userRepository.findByUsername(principal.getName());
        model.addAttribute("user", myUser );
//        model.addAttribute("user", myUser );

        String username = myUser.getUsername();
        model.addAttribute("username", username );

        message.setSentby(username);


//        if (result.hasErrors()  || file.isEmpty()){
//            return "messageform";
//        }
//        if(file.isEmpty()){
//            return "redirect:/add";
//        }

        try{
            Map uploadResult = cloudc.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            message.setImage(uploadResult.get("url").toString());
            messageRepository.save(message);
        }catch(IOException e){
            e.printStackTrace();
            return "redirect:/add";
        }
        return "redirect:/";
    }







    @RequestMapping("/detail/{id}")
    public String showMessage(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findOne(id));
        return "show";
    }

}