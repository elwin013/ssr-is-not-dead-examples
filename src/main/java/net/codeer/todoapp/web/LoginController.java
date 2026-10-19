package net.codeer.todoapp.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.codeer.todoapp.common.LoginDAO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public final class LoginController {
    private final LoginDAO loginDAO;

    public LoginController(LoginDAO loginDAO) {
        this.loginDAO = loginDAO;
    }

    @GetMapping("/login")
    String showLogin(Model model) {
        model.addAttribute("error", "");
        return "login";
    }

    @PostMapping("/login")
    String login(@RequestParam(defaultValue = "") String email,
                 @RequestParam(defaultValue = "") String password,
                 HttpServletRequest request, HttpServletResponse response, Model model) {
        if (email.isBlank() || password.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("error", "Email and password are required.");
            return "login";
        }
        return loginDAO.authenticate(email.trim(), password)
                .map(user -> {
                    request.getSession().setAttribute(WebConfiguration.SESSION_USER, user);
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    model.addAttribute("error", "Invalid email or password.");
                    return "login";
                });
    }

    @PostMapping("/logout")
    String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
