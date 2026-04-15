package mps.logic;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExpressionService {

    private final SuggestionEngine suggestionEngine = new SuggestionEngine();

    public ExpressionResult process(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return new ExpressionResult(null, null, null, buildErrorText("Expression is empty.", "EMPTY"));
        }

        ConversionResult conversion = convertToPostfix(expression);
        if (conversion.errorCode != null) {
            return new ExpressionResult(null, null, buildStepsText(conversion.steps, null),
                    buildErrorText(conversion.errorMessage, conversion.errorCode));
        }

        EvaluationResult evaluation = evaluatePostfix(conversion.postfixTokens, conversion.steps);
        if (evaluation.errorMessage != null) {
            return new ExpressionResult(String.join(" ", conversion.postfixTokens), null,
                    buildStepsText(conversion.steps, evaluation.steps),
                    buildErrorText(evaluation.errorMessage, "MISSING_OPERAND"));
        }

        String resultText = formatNumber(evaluation.value);
        return new ExpressionResult(String.join(" ", conversion.postfixTokens), resultText,
                buildStepsText(conversion.steps, evaluation.steps), "");
    }

    private String buildStepsText(List<String> conversionSteps, List<String> evaluationSteps) {
        StringBuilder builder = new StringBuilder();
        if (conversionSteps != null) {
            builder.append("Infix to Postfix Steps:\n");
            for (String step : conversionSteps) {
                builder.append(step).append("\n");
            }
        }
        if (evaluationSteps != null) {
            builder.append("\nPostfix Evaluation Steps:\n");
            for (String step : evaluationSteps) {
                builder.append(step).append("\n");
            }
        }
        return builder.toString();
    }

    private String buildErrorText(String errorMessage, String errorCode) {
        StringBuilder builder = new StringBuilder();
        builder.append(errorMessage == null ? "" : errorMessage);
        List<String> suggestions = suggestionEngine.suggestionsFor(errorCode);
        if (!suggestions.isEmpty()) {
            builder.append("\nSuggestions:");
            for (String suggestion : suggestions) {
                builder.append("\n- ").append(suggestion);
            }
        }
        return builder.toString();
    }

    private String formatNumber(Double value) {
        if (value == null) {
            return "";
        }
        double rounded = Math.round(value);
        if (Math.abs(value - rounded) < 1e-9) {
            return String.valueOf((long) rounded);
        }
        DecimalFormat formatter = new DecimalFormat("0.########");
        return formatter.format(value);
    }

    private ConversionResult convertToPostfix(String expression) {
        List<String> output = new ArrayList<>();
        Deque<Character> operators = new ArrayDeque<>();
        List<String> steps = new ArrayList<>();

        PrevType previous = PrevType.NONE;
        int index = 0;
        while (index < expression.length()) {
            char current = expression.charAt(index);
            if (Character.isWhitespace(current)) {
                index++;
                continue;
            }

            boolean unary = (current == '-' || current == '+') && (previous == PrevType.NONE || previous == PrevType.OPERATOR || previous == PrevType.OPEN);
            if (Character.isDigit(current) || current == '.' || unary) {
                if (!unary && previous == PrevType.NUMBER) {
                    return ConversionResult.error("MISSING_OPERATOR", "Missing operator between values.", steps);
                }
                if (!unary && previous == PrevType.CLOSE) {
                    if (!pushImplicitMultiplication(operators, output, steps)) {
                        return ConversionResult.error("OPERATOR_MISPLACED", "Invalid implicit operator before number.", steps);
                    }
                }
                int start = index;
                if (unary) {
                    index++;
                }
                boolean hasDigit = false;
                boolean hasDot = false;
                while (index < expression.length()) {
                    char c = expression.charAt(index);
                    if (Character.isDigit(c)) {
                        hasDigit = true;
                        index++;
                        continue;
                    }
                    if (c == '.') {
                        if (hasDot) {
                            break;
                        }
                        hasDot = true;
                        index++;
                        continue;
                    }
                    break;
                }
                if (!hasDigit) {
                    return ConversionResult.error("INVALID_TOKEN", "Invalid number format near '" + expression.substring(start, Math.min(start + 1, expression.length())) + "'.", steps);
                }
                String number = expression.substring(start, index);
                output.add(number);
                steps.add("PUSH number " + number + " to output");
                previous = PrevType.NUMBER;
                continue;
            }

            if (isOpeningBracket(current)) {
                if (previous == PrevType.NUMBER || previous == PrevType.CLOSE) {
                    if (!pushImplicitMultiplication(operators, output, steps)) {
                        return ConversionResult.error("OPERATOR_MISPLACED", "Invalid implicit operator before '" + current + "'.", steps);
                    }
                }
                operators.push(current);
                steps.add("PUSH bracket " + current + " to stack");
                previous = PrevType.OPEN;
                index++;
                continue;
            }

            if (isClosingBracket(current)) {
                if (previous == PrevType.OPERATOR || previous == PrevType.OPEN || previous == PrevType.NONE) {
                    return ConversionResult.error("MISSING_OPERAND", "Missing operand before '" + current + "'.", steps);
                }
                if (!popUntilMatchingBracket(current, operators, output, steps)) {
                    return ConversionResult.error("MISMATCHED_BRACKETS", "Mismatched brackets detected near '" + current + "'.", steps);
                }
                previous = PrevType.CLOSE;
                index++;
                continue;
            }

            if (isOperator(current)) {
                if (previous == PrevType.NONE || previous == PrevType.OPERATOR || previous == PrevType.OPEN) {
                    return ConversionResult.error("OPERATOR_MISPLACED", "Consecutive operators or operator after a bracket detected.", steps);
                }
                while (!operators.isEmpty() && isOperator(operators.peek())
                        && (precedence(operators.peek()) > precedence(current)
                        || (precedence(operators.peek()) == precedence(current) && !isRightAssociative(current)))) {
                    char popped = operators.pop();
                    output.add(String.valueOf(popped));
                    steps.add("POP operator " + popped + " to output");
                }
                operators.push(current);
                steps.add("PUSH operator " + current + " to stack");
                previous = PrevType.OPERATOR;
                index++;
                continue;
            }

            return ConversionResult.error("INVALID_TOKEN", "Invalid token: '" + current + "'.", steps);
        }

        if (previous == PrevType.OPERATOR || previous == PrevType.OPEN) {
            return ConversionResult.error("MISSING_OPERAND", "Expression cannot end with an operator or an opening bracket.", steps);
        }

        while (!operators.isEmpty()) {
            char op = operators.pop();
            if (isOpeningBracket(op)) {
                return ConversionResult.error("UNMATCHED_PARENTHESIS", "Unmatched '" + op + "' detected.", steps);
            }
            output.add(String.valueOf(op));
            steps.add("POP operator " + op + " to output");
        }

        return new ConversionResult(output, steps, null, null);
    }

    private EvaluationResult evaluatePostfix(List<String> postfixTokens, List<String> conversionSteps) {
        List<String> steps = new ArrayList<>();
        Deque<Double> stack = new ArrayDeque<>();
        for (String token : postfixTokens) {
            if (isNumber(token)) {
                double value = Double.parseDouble(token);
                stack.push(value);
                steps.add("PUSH number " + formatNumber(value) + " to stack");
                continue;
            }
            if (token.length() == 1 && isOperator(token.charAt(0))) {
                if (stack.size() < 2) {
                    return new EvaluationResult(null, steps, "Insufficient operands for operator '" + token + "'.");
                }
                double right = stack.pop();
                double left = stack.pop();
                steps.add("POP operands " + formatNumber(left) + ", " + formatNumber(right));
                double result = applyOperator(left, right, token.charAt(0));
                stack.push(result);
                steps.add("PUSH result " + formatNumber(result) + " to stack");
                continue;
            }
            return new EvaluationResult(null, steps, "Invalid token in postfix expression: '" + token + "'.");
        }

        if (stack.size() != 1) {
            return new EvaluationResult(null, steps, "Evaluation ended with unexpected stack size.");
        }
        return new EvaluationResult(stack.pop(), steps, null);
    }

    private boolean popUntilMatchingBracket(char closing, Deque<Character> operators, List<String> output, List<String> steps) {
        while (!operators.isEmpty() && !isOpeningBracket(operators.peek())) {
            char op = operators.pop();
            output.add(String.valueOf(op));
            steps.add("POP operator " + op + " to output");
        }
        if (operators.isEmpty()) {
            return false;
        }
        char opening = operators.pop();
        if (!isMatchingPair(opening, closing)) {
            return false;
        }
        steps.add("POP bracket pair " + opening + " " + closing);
        return true;
    }

    private boolean pushImplicitMultiplication(Deque<Character> operators, List<String> output, List<String> steps) {
        char implicit = '*';
        while (!operators.isEmpty() && isOperator(operators.peek())
                && (precedence(operators.peek()) > precedence(implicit)
                || (precedence(operators.peek()) == precedence(implicit) && !isRightAssociative(implicit)))) {
            char popped = operators.pop();
            output.add(String.valueOf(popped));
            steps.add("POP operator " + popped + " to output");
        }
        operators.push(implicit);
        steps.add("PUSH implicit operator * to stack");
        return true;
    }

    private boolean isOperator(char value) {
        return value == '+' || value == '-' || value == '*' || value == '/' || value == '^';
    }

    private int precedence(char operator) {
        if (operator == '^') {
            return 4;
        }
        if (operator == '*' || operator == '/') {
            return 3;
        }
        if (operator == '+' || operator == '-') {
            return 2;
        }
        return 0;
    }

    private boolean isRightAssociative(char operator) {
        return operator == '^';
    }

    private boolean isOpeningBracket(char value) {
        return value == '(' || value == '{' || value == '[';
    }

    private boolean isClosingBracket(char value) {
        return value == ')' || value == '}' || value == ']';
    }

    private boolean isMatchingPair(char opening, char closing) {
        return (opening == '(' && closing == ')')
                || (opening == '{' && closing == '}')
                || (opening == '[' && closing == ']');
    }

    private boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        int start = 0;
        char first = value.charAt(0);
        if (first == '+' || first == '-') {
            start = 1;
            if (value.length() == 1) {
                return false;
            }
        }
        boolean hasDigit = false;
        boolean hasDot = false;
        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }
            if (c == '.' && !hasDot) {
                hasDot = true;
                continue;
            }
            return false;
        }
        return hasDigit;
    }

    private double applyOperator(double left, double right, char operator) {
        switch (operator) {
            case '+':
                return left + right;
            case '-':
                return left - right;
            case '*':
                return left * right;
            case '/':
                return left / right;
            case '^':
                return Math.pow(left, right);
            default:
                return 0;
        }
    }

    private static class ConversionResult {
        private final List<String> postfixTokens;
        private final List<String> steps;
        private final String errorCode;
        private final String errorMessage;

        private ConversionResult(List<String> postfixTokens, List<String> steps, String errorCode, String errorMessage) {
            this.postfixTokens = postfixTokens;
            this.steps = steps;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        private static ConversionResult error(String errorCode, String errorMessage, List<String> steps) {
            return new ConversionResult(new ArrayList<>(), steps, errorCode, errorMessage);
        }
    }

    private static class EvaluationResult {
        private final Double value;
        private final List<String> steps;
        private final String errorMessage;

        private EvaluationResult(Double value, List<String> steps, String errorMessage) {
            this.value = value;
            this.steps = steps;
            this.errorMessage = errorMessage;
        }
    }

    private enum PrevType {
        NONE,
        NUMBER,
        OPERATOR,
        OPEN,
        CLOSE
    }
}
