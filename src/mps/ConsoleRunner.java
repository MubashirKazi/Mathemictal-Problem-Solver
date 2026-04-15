package mps;

import mps.logic.ExpressionResult;
import mps.logic.ExpressionService;

public class ConsoleRunner {
    public static void main(String[] args) {
        String expression = args.length > 0 ? String.join(" ", args) : "(3+4)*2^2";
        ExpressionService service = new ExpressionService();
        ExpressionResult result = service.process(expression);

        System.out.println("Expression: " + expression);
        if (result.getErrorText() != null && !result.getErrorText().isEmpty()) {
            System.out.println("Error:\n" + result.getErrorText());
            return;
        }
        System.out.println("Postfix: " + result.getPostfix());
        System.out.println("Result: " + result.getResult());
        System.out.println("\nSteps:\n" + result.getStepsText());
    }
}
