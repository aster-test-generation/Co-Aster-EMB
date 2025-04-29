package com.example.trianglespa;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api")
public class FormController {

    @PostMapping("/classify")
    public Map<String, String> classify(@RequestBody TriangleInput input) {
        int result = TriangleClassification.classify(input.getA(), input.getB(), input.getC());
        String type = getTriangleType(result);
        return Map.of("type", type);
    }

    private String getTriangleType(int value) {
        return switch (value) {
            case 0 -> "Not a triangle";
            case 1 -> "Scalene triangle";
            case 2 -> "Isosceles triangle";
            case 3 -> "Equilateral triangle";
            default -> "Invalid input";
        };
    }

    public static class TriangleInput {
        private int a, b, c;
        public int getA() { return a; }
        public int getB() { return b; }
        public int getC() { return c; }
        public void setA(int a) { this.a = a; }
        public void setB(int b) { this.b = b; }
        public void setC(int c) { this.c = c; }
    }
}
