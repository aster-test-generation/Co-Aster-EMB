function classifyTriangle(a, b, c) {
    a = Number(a);
    b = Number(b);
    c = Number(c);

    if (a <= 0 || b <= 0 || c <= 0 || a + b <= c || a + c <= b || b + c <= a) {
        return "not-a-triangle";
    } else if (a === b && b === c) {
        return "equilateral";
    } else if (a === b || b === c || a === c) {
        return "isosceles";
    } else {
        return "scalene";
    }
}

document.getElementById("triangleForm").addEventListener("submit", function (event) {
    event.preventDefault();

    const a = document.getElementById("a").value;
    const b = document.getElementById("b").value;
    const c = document.getElementById("c").value;

    const resultType = classifyTriangle(a, b, c);
    const resultText = `Result: ${resultType.replace(/-/g, " ")}`;

    // Update URL and show result
    history.pushState({ a, b, c }, "", `/${resultType}`);
    const resultEl = document.getElementById("result");
    resultEl.textContent = resultText;
    resultEl.classList.remove("hidden");
});

// Handle back/forward navigation
window.onpopstate = function () {
    const resultEl = document.getElementById("result");
    if (location.pathname === "/") {
        resultEl.classList.add("hidden");
    } else {
        const text = location.pathname.slice(1).replace(/-/g, " ");
        resultEl.textContent = `Result: ${text}`;
        resultEl.classList.remove("hidden");
    }
};
