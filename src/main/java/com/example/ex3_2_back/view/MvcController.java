package com.example.ex3_2_back.view;

import com.example.ex3_2_back.entity.Image;
import com.example.ex3_2_back.entity.Movie;
import com.example.ex3_2_back.entity.User;
import com.example.ex3_2_back.repository.ImageRepository;
import com.example.ex3_2_back.repository.MovieRepository;
import com.example.ex3_2_back.repository.UserRepository;
import com.example.ex3_2_back.security.MySecurity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("/test")
@Slf4j
public class MvcController {
    MySecurity mySecurity;

    UserRepository userRepository;

    MovieRepository movieRepository;

    ImageRepository imageRepository;

    @Autowired
    public void setImageRepository(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Autowired
    public void setMovieRepository(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setMySecurity(MySecurity mySecurity) {
        this.mySecurity = mySecurity;
    }

    @GetMapping({"/index"})
    public String getIndex(Model model) {
        model.addAttribute("testMessage", "Back Server Test");
        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/shipment")
    public String getShipmentPage(Model model) {
        return "shipment";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        return "register";
    }

    @GetMapping("/image")
    public String getImagesPage(Model model) {
        model.addAttribute("imageUploadMessage", "欢迎");
        return "image";
    }

    @PostMapping("/login")
    public String login(Model model, @RequestParam String username, @RequestParam String password, HttpServletResponse response) {

        if (userRepository.existsByNameAndPassword(username, password)) {
            String newToken = mySecurity.genToken(new User());
            var tokenCookie = new Cookie("token", newToken);
            tokenCookie.setAttribute("time", Calendar.getInstance().getTime().toString());
            tokenCookie.setMaxAge(36000);
            response.addCookie(tokenCookie);
            response.addHeader("Access-Control-Allow-Credentials", String.valueOf(true));

            model.addAttribute("loginMessage", "登录成功");

            return "redirect:/test/shipment";
        } else {
            model.addAttribute("loginMessage", "登录失败");
            return "login";
        }
    }

    @PostMapping("/register")
    public String register(Model model, @RequestParam String username, @RequestParam String password, HttpServletResponse response) {

        System.err.println(username + " " + password);


        if (userRepository.existsByNameAndPassword(username, password)) {
            model.addAttribute("registerMessage", "该账户已注册");
            return "register";
        }

        userRepository.save(User.builder().name(username).password(password).build());

        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Movie> movies = movieRepository.findByOrderByVoteAverage(PageRequest.of(0, 10));
        List<Movie> newMovies = movies;
        model.addAttribute("movies", movies);
        model.addAttribute("newMovies", newMovies);
        return "home";
    }


    @PostMapping("/image")
    public String uploadImage(Model model, @RequestParam("file") MultipartFile file) throws Exception {

        // 将 MultipartFile 转换为字节数组
        byte[] data;

        try {
            data = file.getBytes();
        } catch (Exception e) {
            var imageUploadMessage = "读取文件发生异常";
            model.addAttribute("imageUploadMessage", imageUploadMessage);
            log.info(imageUploadMessage);
            return "/image";
        }

        if (data.length > 8 * 1024) {
            var imageUploadMessage = "图片尺寸过大";
            model.addAttribute("imageUploadMessage", imageUploadMessage);
            log.info(imageUploadMessage);
            return "/image";
        }

        // 创建 Image 对象
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setData(data);
        image.setType(file.getContentType());

        // 将 Image 对象保存到数据库中
        imageRepository.save(image);


        model.addAttribute("imageUploadMessage", "上传成功");
        return "/image";
    }

}
