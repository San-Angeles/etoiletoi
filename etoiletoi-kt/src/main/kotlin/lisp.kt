sealed class LispValue {
    data class LispNumber(val value: Double) : LispValue()
    data class LispSymbol(val name: String) : LispValue()
    data class LispList(val values: List<LispValue>) : LispValue()

    //    data class LispLambda(val params: List<LispValue>, val body: LispValue) : LispValue()
//    object LispTrue : LispValue()
//    object LispFalse : LispValue()
    data class LispBool(val value: Boolean) : LispValue()
}


class Environment(private val parent: Environment? = null) {
    val values = mutableMapOf<String, LispValue>()

    fun define(name: String, value: LispValue) {
        values[name] = value
    }

    operator fun get(name: String): LispValue? {
        return values[name]
            ?: parent?.get(name)
    }

    fun isDefined(name: String): Boolean {
        return values.containsKey(name)
    }

    fun set(name: String, value: LispValue) {
        if (!isDefined(name)) {
            throw LispException("$name is not defined in this context")
        }
        values[name] = value
    }
}


fun eval(program: String, env: Environment): LispValue {
    return evaluate(parse(program), env)
}

fun main() {

    val env = getStandardEnv()
    eval("(define x 211)", env)
//    eval("(lambda x x)", env)
//    println(eval("(+ x 9)", env))
    println(eval("(fx 5)", env))

}

//
//class Lambda(private val params: List<LispValue>, private val body: LispValue, private val env: Environment) {
//    fun invoke(args: List<LispValue>): LispValue {
//        val lambdaEnv = Environment(env)
//        for ((index, name) in params.withIndex()) {
//            lambdaEnv.define((name as LispValue.LispSymbol).name, args[index])
//        }
//        return evaluate(body, lambdaEnv)
//    }
//}


data class LispLambda(
    val params: List<LispValue>,
    val body: LispValue,
    val env: Environment
) : LispValue() {
    fun invoke(args: List<LispValue>): LispValue {
        val lambdaEnv = Environment(env)
        for ((index, name) in params.withIndex()) {
            lambdaEnv.define((name as LispSymbol).name, args[index])
        }
        println("lambda-invoke: $body")
        return evaluate(body, lambdaEnv)
    }
}


class LispException(message: String) : Throwable(message)


fun evaluate(expr: LispValue, env: Environment): LispValue {
    return when (expr) {
        is LispValue.LispSymbol -> {
            env[expr.name] ?: throw LispException("Unbound symbol '${expr.name}'")
        }

        is LispValue.LispNumber -> expr

        is LispValue.LispList -> {
//            println("eval list: ${expr.values}")

//            val operands = expr.values.map { evaluate(it, env) }
            var operands = expr.values

            // Is it a function invocation or a special form?
            val head = operands.firstOrNull() ?: throw LispException("Empty list")
            operands = operands.subList(1, operands.size)

            when (head) {
                is LispValue.LispSymbol -> {
//                    println("name: " + head.name)

                    when (head.name) {
                        "quote" -> evalQuote(operands)
                        "if" -> evalIf(operands, env)
                        "define" -> evalDefine(operands, env)
                        "lambda" -> evalLambda(operands, env)
                        "set!" -> evalSet(operands, env)
                        "+" -> evalSum(operands, env)

                        else -> {
                            val operator = evaluate(head, env)
                            if (operator is LispLambda) {
                                operator.invoke(operands)
                            } else {
                                throw LispException("'$head' is not a function")
                            }
                        }
                    }
                }

                is LispLambda -> {
//                    println(operands)
//                    LispValue.LispSymbol(
//                        operands.subList(1, operands.size)
//                            .toString()
//                    )
                    head.invoke(operands.subList(1, operands.size))
                }

                else -> throw LispException("Invalid expression")
            }
        }

        else -> throw LispException("Invalid expression")
    }
}

fun evalSum(operands: List<LispValue>, env: Environment): LispValue {
    println("eval-sum: $operands")
    println("eval-sum-env: " + env.values)
    return LispValue.LispNumber(
        operands.sumOf { x ->
            (evaluate(x, env)
                    as LispValue.LispNumber).value
        },
    )
}


fun addGlobals(env: Environment) {

    env.define(
        "+", LispLambda(
            listOf(LispValue.LispSymbol("x"), LispValue.LispSymbol("y")),
            LispValue.LispList(listOf(LispValue.LispSymbol("+"), LispValue.LispSymbol("x"), LispValue.LispSymbol("y"))),
            env
        )
    )

    env.define(
        "fx", LispLambda(
            listOf(LispValue.LispSymbol("x")),
            LispValue.LispList(
                listOf(
                    LispValue.LispSymbol("+"),
                    LispValue.LispSymbol("x"),
//                    LispValue.LispNumber(1.0)
                    LispValue.LispList(
                        listOf(
                            LispValue.LispSymbol("+"),
                            LispValue.LispSymbol("x"),
                            LispValue.LispNumber(2.0)
                        )
                    )
                )
            ),
            env
        )
    )

    env.define(
        "-", LispLambda(
            listOf(LispValue.LispSymbol("x"), LispValue.LispSymbol("y")),
            LispValue.LispList(listOf(LispValue.LispSymbol("-"), LispValue.LispSymbol("x"), LispValue.LispSymbol("y"))),
            env
        )
    )

    env.define(
        "*", LispLambda(
            listOf(LispValue.LispSymbol("x"), LispValue.LispSymbol("y")),
            LispValue.LispList(listOf(LispValue.LispSymbol("*"), LispValue.LispSymbol("x"), LispValue.LispSymbol("y"))),
            env
        )
    )

}


fun getStandardEnv(): Environment {
    val env = Environment()
    addGlobals(env)
    return env
}


fun parse(program: String): LispValue {
    return parseList(tokenize(program))
}

fun tokenize(program: String): MutableList<String> {
    return program
        .replace("(", " ( ")
        .replace(")", " ) ")
        .split(" ")
        .filter { it.isNotBlank() }
        .toMutableList()
}

fun parseList(tokens: MutableList<String>): LispValue {
    if (tokens.isEmpty()) {
        throw LispException("Unexpected EOF while parsing")
    }

    return when (val token = tokens.first()) {
        "(" -> parseSublist(tokens.subList(1, tokens.size))
        ")" -> throw LispException("Unexpected ')' while parsing")
        else -> parseAtom(token)
    }
}

fun parseSublist(tokens: MutableList<String>): LispValue.LispList {
    val values = mutableListOf<LispValue>()

    while (tokens.firstOrNull() != ")") {
        val value = parseList(tokens)
        values.add(value)
        tokens.removeFirst()
    }

    if (tokens.isEmpty()) {
        throw LispException("Unexpected EOF while parsing")
    }

    return LispValue.LispList(values)
}


fun parseAtom(token: String): LispValue {
    return when {
        token.toDoubleOrNull() != null -> LispValue.LispNumber(token.toDouble())
        else -> LispValue.LispSymbol(token)
    }
}


fun evalQuote(args: List<LispValue>): LispValue {
    if (args.size != 1) {
        throw LispException("quote requires only one argument")
    }

    return args[0]
}


fun evalDefine(args: List<LispValue>, env: Environment): LispValue {

//    println(args)
    if (args.size != 2) {
        throw LispException("define requires only two argument")
    }

    val name = (args[0] as? LispValue.LispSymbol)?.name
        ?: throw LispException("define requires a symbol as its first argument")

    val value = evaluate(args[1], env)
    env.define(name, value)

    return value
}


fun evalLambda(args: List<LispValue>, env: Environment): LispLambda {
    if (args.size != 2) {
        throw LispException("lambda requires two arguments")
    }

    println("lambda-args: $args")
    val parameters = (args[0] as? LispValue.LispList)
        ?.values
//        ?.map {
//            (it as? LispValue.LispSymbol)?.name
//            ?: throw LispException("Invalid argument list")
//        }
        ?: throw LispException("Invalid argument list")

    val body = args[1]

    return LispLambda(parameters, body, env)
}


fun evalIf(args: List<LispValue>, env: Environment): LispValue {
    if (args.size != 3) {
        throw LispException("if requires three arguments")
    }

    val condition = evaluate(args[0], env)
    val thenBranch = args[1]
    val elseBranch = args[2]

    return if (condition != LispValue.LispBool(false)) {
        evaluate(thenBranch, env)
    } else {
        evaluate(elseBranch, env)
    }
}


fun evalSet(args: List<LispValue>, env: Environment): LispValue {
    if (args.size != 2) {
        throw LispException("set! requires only two arguments")
    }

    val name = (args[0] as? LispValue.LispSymbol)?.name
        ?: throw LispException("set! requires a symbol as its first argument")

    if (!env.isDefined(name)) {
        throw LispException("'$name' is not defined")
    }

    val value = evaluate(args[1], env)

    env.set(name, value)

    return value
}

