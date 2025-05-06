function classifyTriangle(a, b, c) {
    a = Number(a);
    b = Number(b);
    c = Number(c);

    if (a <= 0 || b <= 0 || c <= 0 || a + b <= c || a + c <= b || b + c <= a) {
        return "Not a triangle";
    } else if (a === b && b === c) {
        return "Equilateral triangle";
    } else if (a === b || b === c || a === c) {
        return "Isosceles triangle";
    } else {
        return "Scalene triangle";
    }
}

document.getElementById("triangleForm").addEventListener("submit", function (event) {
    event.preventDefault();
    const a = document.getElementById("a").value;
    const b = document.getElementById("b").value;
    const c = document.getElementById("c").value;

    const result = classifyTriangle(a, b, c);
    document.getElementById("result").textContent = `Result: ${result}`;
});
