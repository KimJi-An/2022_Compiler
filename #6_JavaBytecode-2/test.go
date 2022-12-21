func Add(x int, y int) int {
    var z int
    z = x + y
    return z
}

func Mul(x int, y int) int {
    var z int
    z = x * y
    return z
}

func For(min int, max int) {
    var result int = 0

    for min < 2 {
        var i int = 0
        for i < max {
            result = result + 2
            ++i
            --max
        }
        max = 2
        ++min
    }
    _print(result)
}

func AndOr(x int) {
    _print(x and 1)
    _print(x or 1)
}

func main() {
    var x int = 2
    var y int
    y = 10
    _print(Mul(x, y) - Add(x, y))
    _print(Mul(x, y) / 10)
    For(0, 5)
    AndOr(0)
}