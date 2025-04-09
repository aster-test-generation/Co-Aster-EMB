package com.example.springmvcdocker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FormController {

    @GetMapping("/index")
    public String showForm() {
        return "index";
    }

    @PostMapping("/process")
    public String processForm(
            @RequestParam("a") int a,
            @RequestParam("b") int b,
            @RequestParam("c") int c)
    {
        int value = TriangleClassification.classify(a, b, c);
        String triangleType = getTriangleType(value);
        return "redirect:/" + triangleType.toLowerCase().replace(" ", "-");
    }

    @RequestMapping("/not-a-triangle")
    public String notATriangle(Model model) {
        model.addAttribute("output", "The input does not form a triangle.");
        return "notTriangle";
    }

    @RequestMapping("/scalene-triangle")
    public String scaleneTriangle(Model model) {
        model.addAttribute("output", "This is a Scalene triangle.");
        return "scalene";
    }

    @RequestMapping("/isosceles-triangle")
    public String isoscelesTriangle(Model model) {
        model.addAttribute("output", "This is an Isosceles triangle.");
        return "isosceles";
    }

    @RequestMapping("/equilateral-triangle")
    public String equilateralTriangle(Model model) {
        model.addAttribute("output", "This is an Equilateral triangle.");
        return "equilateral";
    }

    public static String getTriangleType(int value) {
        switch (value) {
            case 0: return "Not a triangle";
            case 1: return "Scalene triangle";
            case 2: return "Isosceles triangle";
            case 3: return "Equilateral triangle";
            default: return "Invalid input";
        }
    }
}