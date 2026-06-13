package com.example.data

import java.util.regex.Pattern

object PythonSimulator {

    fun executeCode(code: String): String {
        val lines = code.lines()
        val variables = mutableMapOf<String, Any>()
        val outputBuilder = StringBuilder()
        
        var inIfBlock = false
        var ifConditionMet = false
        var currentIndent = 0
        
        var inLoopBlock = false
        var loopVarName = ""
        var loopItems = listOf<Any>()
        var loopCodeLines = mutableListOf<String>()

        try {
            var i = 0
            while (i < lines.size) {
                val originLine = lines[i]
                val trimmed = originLine.trim()
                
                // Skip comments and empty lines
                if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                    i++
                    continue
                }

                // Check indent to see if we exited or remained in a block
                val lineIndent = originLine.takeWhile { it == ' ' }.length
                
                // Handle IF Block exit
                if (inIfBlock && lineIndent <= currentIndent && trimmed.isNotEmpty() && !trimmed.startsWith("else") && !trimmed.startsWith("elif")) {
                    inIfBlock = false
                }
                
                // Handle Loop Block exit and execution
                if (inLoopBlock && lineIndent <= currentIndent && trimmed.isNotEmpty()) {
                    runLoop(loopVarName, loopItems, loopCodeLines, variables, outputBuilder)
                    inLoopBlock = false
                    loopCodeLines.clear()
                }

                // If inside a block that shouldn't execute
                if (inIfBlock && !ifConditionMet) {
                    i++
                    continue
                }

                // If collecting loop lines
                if (inLoopBlock) {
                    loopCodeLines.add(originLine)
                    i++
                    continue
                }

                // IF Condition structure
                if (trimmed.startsWith("if ") && trimmed.endsWith(":")) {
                    inIfBlock = true
                    currentIndent = originLine.takeWhile { it == ' ' }.length
                    val condExpr = trimmed.substring(3, trimmed.length - 1).trim()
                    ifConditionMet = evaluateCondition(condExpr, variables)
                    i++
                    continue
                }

                // ELIF structure
                if (trimmed.startsWith("elif ") && trimmed.endsWith(":")) {
                    currentIndent = originLine.takeWhile { it == ' ' }.length
                    if (inIfBlock && !ifConditionMet) {
                        val condExpr = trimmed.substring(5, trimmed.length - 1).trim()
                        ifConditionMet = evaluateCondition(condExpr, variables)
                    } else {
                        ifConditionMet = true // Already executed or skipped
                    }
                    i++
                    continue
                }

                // ELSE structure
                if (trimmed.startsWith("else") && trimmed.endsWith(":")) {
                    currentIndent = originLine.takeWhile { it == ' ' }.length
                    if (inIfBlock) {
                        ifConditionMet = !ifConditionMet
                    }
                    i++
                    continue
                }

                // FOR Loop structure
                if (trimmed.startsWith("for ") && trimmed.contains(" in ") && trimmed.endsWith(":")) {
                    currentIndent = originLine.takeWhile { it == ' ' }.length
                    val forParts = trimmed.substring(4, trimmed.length - 1).split(" in ")
                    if (forParts.size == 2) {
                        loopVarName = forParts[0].trim()
                        val containerExpr = forParts[1].trim()
                        loopItems = parseContainerExpression(containerExpr, variables)
                        inLoopBlock = true
                        loopCodeLines = mutableListOf()
                    }
                    i++
                    continue
                }

                // DEF function declaration (simple mock print info or return skip)
                if (trimmed.startsWith("def ") && trimmed.endsWith(":")) {
                    val declName = trimmed.substring(4, trimmed.indexOf('(')).trim()
                    outputBuilder.append("🔧 تابع '$declName' با موفقیت تغریف شد.\n")
                    // Skip lines of function definition
                    val defIndent = originLine.takeWhile { it == ' ' }.length
                    i++
                    while (i < lines.size) {
                        val nextLine = lines[i]
                        if (nextLine.trim().isNotEmpty() && nextLine.takeWhile { it == ' ' }.length <= defIndent) {
                            break
                        }
                        i++
                    }
                    continue
                }

                // Regular execution lines
                executeSingleLine(trimmed, variables, outputBuilder)
                i++
            }

            // If file ended but loop is still pending
            if (inLoopBlock && loopCodeLines.isNotEmpty()) {
                runLoop(loopVarName, loopItems, loopCodeLines, variables, outputBuilder)
            }

        } catch (e: Exception) {
            outputBuilder.append("🚨 خطای پایتون: ${e.localizedMessage ?: "مترجم قادر به پردازش این خط نیست"}\n")
        }

        val finalOut = outputBuilder.toString()
        return if (finalOut.isEmpty()) "برنامه با موفقیت و بدون خروجی اجرا شد." else finalOut
    }

    private fun executeSingleLine(trimmedLine: String, variables: MutableMap<String, Any>, outputBuilder: StringBuilder) {
        // Handle custom append for simulated lists
        // e.g. colors.append("yellow")
        if (trimmedLine.contains(".append(") && trimmedLine.endsWith(")")) {
            val listName = trimmedLine.substringBefore(".append(").trim()
            var rawVal = trimmedLine.substringAfter(".append(").dropLast(1).trim()
            val parsedVal = evalValue(rawVal, variables)
            val existingList = variables[listName]
            if (existingList is MutableList<*>) {
                val castList = existingList as MutableList<Any>
                castList.add(parsedVal)
            }
            return
        }

        // Print Statement evaluation
        if (trimmedLine.startsWith("print(") && trimmedLine.endsWith(")")) {
            val innerExpr = trimmedLine.substring(6, trimmedLine.length - 1).trim()
            val result = evaluateStringConcat(innerExpr, variables)
            outputBuilder.append(result).append("\n")
            return
        }

        // Variable assignment
        if (trimmedLine.contains("=")) {
            val leftSide = trimmedLine.substringBefore("=").trim()
            val rightSide = trimmedLine.substringAfter("=").trim()
            
            // Check list definition
            if (rightSide.startsWith("[") && rightSide.endsWith("]")) {
                val listItemsString = rightSide.substring(1, rightSide.length - 1).trim()
                val list = mutableListOf<Any>()
                if (listItemsString.isNotEmpty()) {
                    listItemsString.split(",").forEach { item ->
                        list.add(evalValue(item.trim(), variables))
                    }
                }
                variables[leftSide] = list
            } else {
                variables[leftSide] = evalValue(rightSide, variables)
            }
        }
    }

    private fun evaluateStringConcat(expr: String, variables: Map<String, Any>): String {
        // Handles expressions of form: "Hello " + name + " and " + str(age)
        val parts = expr.split("+")
        val result = StringBuilder()
        
        for (part in parts) {
            val trimmedPart = part.trim()
            if (trimmedPart.isEmpty()) continue
            
            // If it is literal string
            if ((trimmedPart.startsWith("\"") && trimmedPart.endsWith("\"")) || 
                (trimmedPart.startsWith("\'") && trimmedPart.endsWith("\'"))) {
                result.append(trimmedPart.substring(1, trimmedPart.length - 1))
            } 
            // If str(variable) casting
            else if (trimmedPart.startsWith("str(") && trimmedPart.endsWith(")")) {
                val innerVar = trimmedPart.substring(4, trimmedPart.length - 1).trim()
                val value = variables[innerVar] ?: evalValue(innerVar, variables)
                result.append(value.toString())
            } 
            // If length helper len(...)
            else if (trimmedPart.startsWith("len(") && trimmedPart.endsWith(")")) {
                val innerVar = trimmedPart.substring(4, trimmedPart.length - 1).trim()
                val list = variables[innerVar]
                if (list is List<*>) {
                    result.append(list.size)
                } else {
                    result.append(innerVar.length)
                }
            }
            // List index access, e.g. colors[1]
            else if (trimmedPart.contains("[") && trimmedPart.endsWith("]")) {
                val listName = trimmedPart.substringBefore("[").trim()
                val indexStr = trimmedPart.substringAfter("[").dropLast(1).trim()
                val indexVal = indexStr.toIntOrNull() ?: 0
                val list = variables[listName]
                if (list is List<*>) {
                    if (indexVal in 0 until list.size) {
                        result.append(list[indexVal].toString())
                    } else {
                        result.append("[خطای اندیس]")
                    }
                } else {
                    result.append("[خطا]")
                }
            }
            // Just a variable name or literal
            else {
                val value = variables[trimmedPart] ?: trimmedPart
                result.append(value.toString())
            }
        }
        
        return result.toString()
    }

    private fun evalValue(expr: String, variables: Map<String, Any>): Any {
        val trimmed = expr.trim()
        if (trimmed == "True") return true
        if (trimmed == "False") return false
        
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || 
            (trimmed.startsWith("\'") && trimmed.endsWith("\'"))) {
            return trimmed.substring(1, trimmed.length - 1)
        }
        
        val intVal = trimmed.toIntOrNull()
        if (intVal != null) return intVal
        
        val floatVal = trimmed.toDoubleOrNull()
        if (floatVal != null) return floatVal

        if (trimmed.contains("[") && trimmed.endsWith("]")) {
            val listName = trimmed.substringBefore("[").trim()
            val indexStr = trimmed.substringAfter("[").dropLast(1).trim()
            val indexVal = indexStr.toIntOrNull() ?: 0
            val list = variables[listName]
            if (list is List<*>) {
                if (indexVal in 0 until list.size) {
                    return list[indexVal] ?: ""
                }
            }
        }
        
        return variables[trimmed] ?: trimmed
    }

    private fun evaluateCondition(expr: String, variables: Map<String, Any>): Boolean {
        // matches left op right
        val operators = listOf(">=", "<=", "==", ">", "<", "!=")
        for (op in operators) {
            if (expr.contains(op)) {
                val parts = expr.split(op)
                if (parts.size == 2) {
                    val leftVal = evalValue(parts[0].trim(), variables)
                    val rightVal = evalValue(parts[1].trim(), variables)
                    
                    val leftNum = leftVal.toString().toDoubleOrNull()
                    val rightNum = rightVal.toString().toDoubleOrNull()
                    
                    if (leftNum != null && rightNum != null) {
                        return when (op) {
                            ">=" -> leftNum >= rightNum
                            "<=" -> leftNum <= rightNum
                            "==" -> leftNum == rightNum
                            ">" -> leftNum > rightNum
                            "<" -> leftNum < rightNum
                            "!=" -> leftNum != rightNum
                            else -> false
                        }
                    } else {
                        return when (op) {
                            "==" -> leftVal == rightVal
                            "!=" -> leftVal != rightVal
                            else -> false
                        }
                    }
                }
            }
        }
        
        // Single boolean variable
        val rVal = evalValue(expr, variables)
        if (rVal is Boolean) return rVal
        return rVal.toString().isNotEmpty()
    }

    private fun parseContainerExpression(expr: String, variables: Map<String, Any>): List<Any> {
        val trimmed = expr.trim()
        
        // Match range(start, end, step) or range(end)
        if (trimmed.startsWith("range(") && trimmed.endsWith(")")) {
            val argsRaw = trimmed.substring(6, trimmed.length - 1).split(",")
            val args = argsRaw.map { it.trim().toIntOrNull() ?: 0 }
            val list = mutableListOf<Any>()
            
            if (args.size == 1) {
                for (x in 0 until args[0]) {
                    list.add(x)
                }
            } else if (args.size == 2) {
                for (x in args[0] until args[1]) {
                    list.add(x)
                }
            } else if (args.size == 3) {
                val start = args[0]
                val end = args[1]
                val step = args[2]
                if (step > 0) {
                    var x = start
                    while (x < end) {
                        list.add(x)
                        x += step
                    }
                } else if (step < 0) {
                    var x = start
                    while (x > end) {
                        list.add(x)
                        x += step
                    }
                }
            }
            return list
        }
        
        // Is variable representing list
        val obj = variables[trimmed]
        if (obj is List<*>) {
            return obj.filterNotNull()
        }
        
        // Literal list
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            val listItemsString = trimmed.substring(1, trimmed.length - 1).trim()
            val list = mutableListOf<Any>()
            if (listItemsString.isNotEmpty()) {
                listItemsString.split(",").forEach { item ->
                    list.add(evalValue(item.trim(), variables))
                }
            }
            return list
        }
        
        return emptyList()
    }

    private fun runLoop(
        varName: String, 
        items: List<Any>, 
        codeLines: List<String>, 
        variables: MutableMap<String, Any>,
        outputBuilder: StringBuilder
    ) {
        val previousVal = variables[varName]
        for (item in items) {
            variables[varName] = item
            for (line in codeLines) {
                executeSingleLine(line.trim(), variables, outputBuilder)
            }
        }
        if (previousVal != null) {
            variables[varName] = previousVal
        } else {
            variables.remove(varName)
        }
    }
}
